package almhirt.corex.mongo.eventlog

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.common.LocalDateTimeRange._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.converters.BinaryConverter
import almhirt.configuration._
import almhirt.eventlog._
import almhirt.context._
import almhirt.akkax._
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType
import play.api.libs.iteratee._

object MongoEventLog {
  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeEvent: Event ⇒ AlmValidation[BSONDocument],
    deserializeEvent: BSONDocument ⇒ AlmValidation[Event],
    writeWarnThreshold: FiniteDuration,
    circuitControlSettings: CircuitControlSettings,
    readOnlySettings: Option[RetrySettings])(implicit ctx: AlmhirtContext): Props =
    Props(new MongoEventLogImpl(
      db,
      collectionName,
      serializeEvent,
      deserializeEvent,
      writeWarnThreshold,
      circuitControlSettings,
      readOnlySettings))

  def propsWithDb(
    db: DB with DBMetaCommands,
    serializeEvent: Event ⇒ AlmValidation[BSONDocument],
    deserializeEvent: BSONDocument ⇒ AlmValidation[Event],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.event-logs.event-log" + configName.map("." + _).getOrElse("")
    for {
      section <- ctx.config.v[com.typesafe.config.Config](path)
      collectionName <- section.v[String]("collection-name")
      writeWarnThreshold <- section.v[FiniteDuration]("write-warn-threshold")
      circuitControlSettings <- section.v[CircuitControlSettings]("circuit-control")
      readOnly <- section.v[Boolean]("read-only")
      readOnlySettings <- if (readOnly) {
        section.v[RetrySettings]("read-only-collection-lookup-retries").map(Some(_))
      } else {
        None.success
      }
    } yield propsRaw(
      db,
      collectionName,
      serializeEvent,
      deserializeEvent,
      writeWarnThreshold,
      circuitControlSettings,
      readOnlySettings)
  }

  def propsWithConnection(
    connection: MongoConnection,
    serializeEvent: Event ⇒ AlmValidation[BSONDocument],
    deserializeEvent: BSONDocument ⇒ AlmValidation[Event],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.event-logs.event-log" + configName.map("." + _).getOrElse("")
    for {
      section <- ctx.config.v[com.typesafe.config.Config](path)
      dbName <- section.v[String]("db-name")
      db <- inTryCatch { connection(dbName)(ctx.futuresContext) }
      props <- propsWithDb(
        db,
        serializeEvent,
        deserializeEvent,
        configName)
    } yield props
  }
}

private[almhirt] class MongoEventLogImpl(
  db: DB with DBMetaCommands,
  collectionName: String,
  serializeEvent: Event ⇒ AlmValidation[BSONDocument],
  deserializeEvent: BSONDocument ⇒ AlmValidation[Event],
  writeWarnThreshold: FiniteDuration,
  circuitControlSettings: CircuitControlSettings,
  readOnlySettings: Option[RetrySettings])(implicit override val almhirtContext: AlmhirtContext) extends Actor with ActorLogging with HasAlmhirtContext {
  import EventLog._
  import almhirt.corex.mongo.BsonConverter._
  import almhirt.herder.HerderMessage

  implicit val defaultExecutor = almhirtContext.futuresContext
  val serializationExecutor = almhirtContext.futuresContext

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, almhirtContext.futuresContext, context.system.scheduler)

  val projectionFilter = BSONDocument("event" -> 1)

  val noSorting = BSONDocument()
  val sortByTimestamp = BSONDocument("timestamp" -> 1)

  private val fromBsonDocToEvent: Enumeratee[BSONDocument, Event] =
    Enumeratee.mapM[BSONDocument] { doc ⇒ scala.concurrent.Future { documentToEvent(doc).resultOrEscalate }(serializationExecutor) }

  def eventToDocument(event: Event): AlmValidation[BSONDocument] = {
    (for {
      serialized <- serializeEvent(event)
    } yield {
      BSONDocument(
        ("_id" -> BSONString(event.eventId.value)),
        ("timestamp" -> localDateTimeToBsonDateTime(event.timestamp)),
        ("type" -> BSONString(event.getClass().getSimpleName())),
        ("event" -> serialized))
    }).leftMap(p ⇒ SerializationProblem(s"""Could not serialize a "${event.getClass().getName()}".""", cause = Some(p)))
  }

  def documentToEvent(document: BSONDocument): AlmValidation[Event] = {
    document.get("event") match {
      case Some(d: BSONDocument) ⇒ deserializeEvent(d)
      case Some(x) ⇒ MappingProblem(s"""Event must be contained as a BSONDocument. It is a "${x.getClass().getName()}".""").failure
      case None ⇒ NoSuchElementProblem("BSONDocument for payload not found").failure
    }
  }

  def insertDocument(document: BSONDocument): AlmFuture[Deadline] = {
    val collection = db(collectionName)
    val start = Deadline.now
    for {
      lastError <- collection.insert(document).toAlmFuture
      _ <- if (lastError.ok)
        AlmFuture.successful(())
      else {
        val msg = lastError.errMsg.getOrElse("unknown error")
        AlmFuture.failed(PersistenceProblem(msg))
      }
    } yield start
  }

  def storeEvent(event: Event): AlmFuture[Deadline] =
    for {
      serialized <- AlmFuture { eventToDocument(event: Event) }(serializationExecutor)
      start <- insertDocument(serialized)
    } yield start

  def commitEvent(event: Event, respondTo: Option[ActorRef]) {
    circuitBreaker.fused(storeEvent(event)) onComplete (
      fail ⇒ {
        val msg = s"Could not log event with id ${event.eventId.value}:\n$fail"

        almhirtContext.tellHerder(HerderMessage.MissedEvent(event, MajorSeverity, fail, almhirtContext.getUtcTimestamp))

        respondTo match {
          case Some(r) =>
            log.warning(msg)
            r ! EventNotLogged(event.eventId, PersistenceProblem(msg, cause = Some(fail)))
          case None =>
            log.error(msg)
        }
      },
      start ⇒ {
        val lap = start.lap
        if (lap > writeWarnThreshold)
          log.warning(s"""Storing event "${event.getClass().getSimpleName()}(${event.eventId})" took longer than ${writeWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
        respondTo.foreach(_ ! EventLogged(event.eventId))
      })
  }

  def createQuery(dateRange: LocalDateTimeRange): BSONDocument = {
    dateRange match {
      case LocalDateTimeRange(BeginningOfTime, EndOfTime) =>
        BSONDocument()

      case LocalDateTimeRange(From(from), EndOfTime) =>
        BSONDocument(
          "timestamp" -> BSONDocument("$gte" -> localDateTimeToBsonDateTime(from)))

      case LocalDateTimeRange(After(after), EndOfTime) =>
        BSONDocument(
          "timestamp" -> BSONDocument("$gt" -> localDateTimeToBsonDateTime(after)))

      case LocalDateTimeRange(BeginningOfTime, To(to)) =>
        BSONDocument(
          "timestamp" -> BSONDocument("$lte" -> localDateTimeToBsonDateTime(to)))

      case LocalDateTimeRange(BeginningOfTime, Until(until)) =>
        BSONDocument(
          "timestamp" -> BSONDocument("$lt" -> localDateTimeToBsonDateTime(until)))

      case LocalDateTimeRange(From(from), To(to)) =>
        BSONDocument(
          "$and" -> BSONDocument(
            "timestamp" -> BSONDocument(
              "$gte" -> localDateTimeToBsonDateTime(from)),
            "timestamp" -> BSONDocument(
              "$lte" -> localDateTimeToBsonDateTime(to))))

      case LocalDateTimeRange(From(from), Until(until)) =>
        BSONDocument(
          "$and" -> BSONDocument(
            "timestamp" -> BSONDocument(
              "$gte" -> localDateTimeToBsonDateTime(from)),
            "timestamp" -> BSONDocument(
              "$lt" -> localDateTimeToBsonDateTime(until))))

      case LocalDateTimeRange(After(after), To(to)) =>
        BSONDocument(
          "$and" -> BSONDocument(
            "timestamp" -> BSONDocument(
              "$gt" -> localDateTimeToBsonDateTime(after)),
            "timestamp" -> BSONDocument(
              "$lte" -> localDateTimeToBsonDateTime(to))))

      case LocalDateTimeRange(After(after), Until(until)) =>
        BSONDocument(
          "$and" -> BSONDocument(
            "timestamp" -> BSONDocument(
              "$gt" -> localDateTimeToBsonDateTime(after)),
            "timestamp" -> BSONDocument(
              "$lt" -> localDateTimeToBsonDateTime(until))))
    }
  }

  def getEvents(query: BSONDocument, sort: BSONDocument, traverse: TraverseWindow): Enumerator[Event] = {
    val (skip, take) = traverse.toInts
    val collection = db(collectionName)
    val enumerator = collection.find(query, projectionFilter).options(new QueryOpts(skipN = skip)).sort(sort).cursor.enumerate(maxDocs = take, stopOnError = true)
    enumerator.through(fromBsonDocToEvent)
  }

  def fetchAndDispatchEvents(m: FetchEvents, respondTo: ActorRef) {
    val query = createQuery(m.range)
    respondTo ! FetchedEvents(getEvents(query, sortByTimestamp, m.traverse))
  }

  private case object Initialize
  private case object Initialized
  private case class InitializeFailed(prob: Problem)

  def uninitializedReadWrite: Receive = {
    case Initialize ⇒
      log.info("Initializing(read/write)")
      val collection = db(collectionName)
      (for {
        idxRes <- collection.indexesManager.ensure(MIndex(List("timestamp" -> IndexType.Ascending), name = Some("idx_timestamp"), unique = false))
      } yield (idxRes)).toAlmFuture.onComplete(
        problem ⇒ self ! InitializeFailed(problem),
        idxRes => {
          log.info(s"""Index on "timestamp" created: $idxRes""")
          self ! Initialized
        })
    case Initialized ⇒
      log.info("Initialized")
      almhirtContext.tellHerder(HerderMessage.RegisterCircuitControl(self, circuitBreaker))
      context.become(receiveEventLogMsg(false))

    case InitializeFailed(prob) ⇒
      log.error(s"Initialize failed:\n$prob")
      sys.error(prob.message)

    case LogEvent(event, acknowledge) ⇒
      log.warning(s"""Received event ${event.getClass().getSimpleName()} while uninitialized.""")
      almhirtContext.tellHerder(HerderMessage.MissedEvent(event, MajorSeverity, ServiceNotAvailableProblem("Uninitialized."), almhirtContext.getUtcTimestamp))

    case m: EventLogMessage ⇒
      log.warning(s"""Received event log message ${m.getClass().getSimpleName()} while uninitialized.""")
  }

  def uninitializedReadOnly(collectionLookupRetries: RetrySettings): Receive = {
    case Initialize ⇒
      log.info("Initializing(read-only)")
      context.retryWithLogging[Unit](
        s"Find collection $collectionName",
        () => db.collectionNames.toAlmFuture.foldV(
          fail => fail.failure,
          collectionNames => {
            if (collectionNames.contains(collectionName))
              ().success
            else
              MandatoryDataProblem(s"""Collection "$collectionName" is not among [${collectionNames.mkString(", ")}] in database "${db.name}".""").failure
          }),
        _ => { self ! Initialized },
        (t, n, p) => {
          val prob = MandatoryDataProblem(s"Look up collection '$collectionName' finally failed after $n attempts and ${t.defaultUnitString}.", cause = Some(p))
          self ! InitializeFailed(UnspecifiedProblem(s""))
        },
        this.log,
        collectionLookupRetries,
        Some("looks-for-collection"))

    case Initialized ⇒
      log.info("Initialized")
      almhirtContext.tellHerder(HerderMessage.RegisterCircuitControl(self, circuitBreaker))
      context.become(receiveEventLogMsg(true))

    case InitializeFailed(prob) ⇒
      log.error(s"Initialize failed:\n$prob")
      sys.error(prob.message)

    case LogEvent(event, acknowledge) ⇒
      log.warning(s"""Received event ${event.getClass().getSimpleName()} while uninitialized.""")
      almhirtContext.tellHerder(HerderMessage.MissedEvent(event, MajorSeverity, ServiceNotAvailableProblem("Uninitialized."), almhirtContext.getUtcTimestamp))
      
    case m: EventLogMessage ⇒
      log.warning(s"""Received event log message ${m.getClass().getSimpleName()} while uninitialized.""")
  }

  def receiveEventLogMsg(readOnly: Boolean): Receive = {
    case LogEvent(event, acknowledge) ⇒
      if (readOnly)
        if (!acknowledge)
          log.warning("Received log")
        else
          sender() ! EventNotLogged(event.eventId, IllegalOperationProblem("The event log is in read only mode."))
      else
        commitEvent(event, if (acknowledge) Some(sender()) else None)

    case FindEvent(eventId) ⇒
      val pinnedSender = sender
      val collection = db(collectionName)
      val query = BSONDocument("_id" -> BSONString(eventId.value))
      val res =
        for {
          docs <- collection.find(query).cursor.collect[List](2, true).toAlmFuture
          Event <- AlmFuture {
            docs match {
              case Nil ⇒ None.success
              case d :: Nil ⇒ documentToEvent(d).map(Some(_))
              case x ⇒ PersistenceProblem(s"""Expected 1 event with id "$eventId" but found ${x.size}.""").failure
            }
          }(serializationExecutor)
        } yield Event
      res.onComplete(
        problem ⇒ {
          pinnedSender ! FindEventFailed(eventId, problem)
          log.error(problem.toString())
        },
        eventOpt ⇒ pinnedSender ! FoundEvent(eventId, eventOpt))

    case m: FetchEvents =>
      fetchAndDispatchEvents(m, sender())

  }

  override def receive =
    readOnlySettings match {
      case Some(roSettings) =>
        uninitializedReadOnly(roSettings)
      case None =>
        uninitializedReadWrite
    }

  override def preStart() {
    super.preStart()
    self ! Initialize
  }

  override def postStop() {
    almhirtContext.tellHerder(HerderMessage.DeregisterCircuitControl(self))
  }

}