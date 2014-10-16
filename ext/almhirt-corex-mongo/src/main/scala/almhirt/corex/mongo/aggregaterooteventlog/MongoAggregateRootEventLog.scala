package almhirt.corex.mongo.aggregaterooteventlog

import scala.language.reflectiveCalls
import scala.concurrent._
import scala.concurrent.duration._
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates.AggregateRootId
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.converters.BinaryConverter
import almhirt.configuration._
import almhirt.context._
import almhirt.akkax._
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType
import reactivemongo.core.commands.GetLastError
import com.typesafe.config.Config
import play.api.libs.iteratee._

object MongoAggregateRootEventLog {
  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    writeWarnThreshold: FiniteDuration,
    readWarnThreshold: FiniteDuration,
    circuitControlSettings: CircuitControlSettings,
    retrySettings: RetrySettings,
    readOnly: Boolean)(implicit ctx: AlmhirtContext): Props =
    Props(new MongoAggregateRootEventLogImpl(
      db,
      collectionName,
      serializeAggregateRootEvent,
      deserializeAggregateRootEvent,
      writeWarnThreshold,
      readWarnThreshold,
      circuitControlSettings,
      retrySettings,
      readOnly))

  def propsWithDb(
    db: DB with DBMetaCommands,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.event-logs.aggregate-root-event-log" + configName.map("." + _).getOrElse("")
    for {
      section <- ctx.config.v[com.typesafe.config.Config](path)
      collectionName <- section.v[String]("collection-name")
      writeWarnThreshold <- section.v[FiniteDuration]("write-warn-threshold")
      readWarnThreshold <- section.v[FiniteDuration]("read-warn-threshold")
      circuitControlSettings <- section.v[CircuitControlSettings]("circuit-control")
      retrySettings <- section.v[RetrySettings]("retry-settings")
      readOnly <- section.v[Boolean]("read-only")
    } yield propsRaw(
      db,
      collectionName,
      serializeAggregateRootEvent,
      deserializeAggregateRootEvent,
      writeWarnThreshold,
      readWarnThreshold,
      circuitControlSettings,
      retrySettings,
      readOnly)
  }

  def propsWithConnection(
    connection: MongoConnection,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.event-logs.aggregate-root-event-log" + configName.map("." + _).getOrElse("")
    for {
      section <- ctx.config.v[com.typesafe.config.Config](path)
      dbName <- section.v[String]("db-name")
      db <- inTryCatch { connection(dbName)(ctx.futuresContext) }
      props <- propsWithDb(
        db,
        serializeAggregateRootEvent,
        deserializeAggregateRootEvent,
        configName)
    } yield props
  }

}

private[almhirt] class MongoAggregateRootEventLogImpl(
  db: DB with DBMetaCommands,
  collectionName: String,
  serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
  deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
  writeWarnThreshold: FiniteDuration,
  readWarnThreshold: FiniteDuration,
  circuitControlSettings: CircuitControlSettings,
  retrySettings: RetrySettings,
  readOnly: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with ActorLogging with almhirt.akkax.AlmActorSupport {

  import almhirt.eventlog.AggregateRootEventLog._

  implicit val defaultExecutor = almhirtContext.futuresContext
  val serializationExecutor = almhirtContext.futuresContext

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, almhirtContext.futuresContext, context.system.scheduler)

  val projectionFilter = BSONDocument("event" -> 1)

  val noSorting = BSONDocument()
  val sortByVersion = BSONDocument("aggid" -> 1, "version" -> 1)

  private val fromBsonDocToAggregateRootEvent: Enumeratee[BSONDocument, AggregateRootEvent] =
    Enumeratee.mapM[BSONDocument] { doc ⇒ Future { documentToAggregateRootEvent(doc).resultOrEscalate }(serializationExecutor) }

  def aggregateRootEventToDocument(aggregateRootEvent: AggregateRootEvent): AlmValidation[BSONDocument] = {
    (for {
      serialized <- serializeAggregateRootEvent(aggregateRootEvent)
    } yield {
      BSONDocument(
        ("_id" -> BSONString(aggregateRootEvent.eventId.value)),
        ("aggid" -> BSONString(aggregateRootEvent.aggId.value)),
        ("version" -> BSONLong(aggregateRootEvent.aggVersion.value)),
        ("type" -> BSONString(aggregateRootEvent.getClass().getSimpleName())),
        ("event" -> serialized))
    }).leftMap(p ⇒ SerializationProblem(s"""Could not serialize a "${aggregateRootEvent.getClass().getName()}".""", cause = Some(p)))
  }

  def documentToAggregateRootEvent(document: BSONDocument): AlmValidation[AggregateRootEvent] = {
    (document.get("event") match {
      case Some(d: BSONDocument) ⇒
        deserializeAggregateRootEvent(d)
      case Some(x) ⇒
        val msg = s"""Event must be contained as a BSONDocument. It is a "${x.getClass().getName()}"."""
        MappingProblem(msg).failure
      case None ⇒
        NoSuchElementProblem("BSONDocument for payload not found").failure
    }).leftMap { p ⇒
      val prob = MappingProblem("Could not deserialize BSONDocument to domain event.", cause = Some(p))
      log.error(prob.toString)
      prob
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

  def storeEvent(event: AggregateRootEvent): AlmFuture[Deadline] =
    for {
      serialized <- AlmFuture { aggregateRootEventToDocument(event: AggregateRootEvent) }(serializationExecutor)
      start <- insertDocument(serialized)
    } yield start

  def commitEvent(event: AggregateRootEvent, respondTo: ActorRef) {
    circuitBreaker.fused(storeEvent(event)) onComplete (
      fail ⇒ {
        log.error(s"Could not commit aggregate root event:\n$fail")
        reportMissedEvent(event, CriticalSeverity, fail)
        reportMajorFailure(fail)
        respondTo ! AggregateRootEventNotCommitted(event.eventId, fail)
      },
      start ⇒ {
        respondTo ! AggregateRootEventCommitted(event.eventId)
        val lap = start.lap
        if (lap > writeWarnThreshold)
          log.warning(s"""Storing aggregate root event "${event.getClass().getSimpleName()}(${event.eventId})" took longer than ${writeWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
      })
  }

  def getAggregateRootEventsDocs(query: BSONDocument, sort: BSONDocument, traverse: TraverseWindow): Enumerator[BSONDocument] = {
    val (skip, take) = traverse.toInts
    val collection = db(collectionName)
    val enumerator = collection.find(query, projectionFilter).options(new QueryOpts(skipN = skip)).sort(sort).cursor.enumerate(maxDocs = take, stopOnError = true)
    enumerator
  }

  def getAggregateRootEvents(query: BSONDocument, sort: BSONDocument, traverse: TraverseWindow): Enumerator[AggregateRootEvent] = {
    val docsEnumerator = getAggregateRootEventsDocs(query, sort, traverse)
    docsEnumerator.through(fromBsonDocToAggregateRootEvent)
  }

  def createQueryAndSort(m: AggregateRootEventLogQueryManyMessage): (BSONDocument, BSONDocument) = {
    m match {
      case m: GetAllAggregateRootEvents =>
        (BSONDocument.empty, BSONDocument.empty)
      case GetAggregateRootEventsFor(aggId, start, end, _) =>
        (start, end) match {
          case (FromStart, ToEnd) =>
            (BSONDocument("aggid" -> BSONString(aggId.value)), sortByVersion)
          case (FromVersion(fromVersion), ToEnd) =>
            (BSONDocument(
              "aggid" -> BSONString(aggId.value),
              "version" -> BSONDocument("$gte" -> BSONLong(fromVersion.value))), sortByVersion)
          case (FromStart, ToVersion(toVersion)) =>
            (BSONDocument(
              "aggid" -> BSONString(aggId.value),
              "version" -> BSONDocument("$lte" -> BSONLong(toVersion.value))), sortByVersion)
          case (FromVersion(fromVersion), ToVersion(toVersion)) =>
            (BSONDocument(
              "aggid" -> BSONString(aggId.value),
              "$and" -> BSONArray(
                BSONDocument("version" -> BSONDocument("$gte" -> BSONLong(fromVersion.value))),
                BSONDocument("version" -> BSONDocument("$lte" -> BSONLong(toVersion.value))))), sortByVersion)
        }
    }
  }

  def fetchAndDispatchAggregateRootEvents(m: AggregateRootEventLogQueryManyMessage, respondTo: ActorRef) {
    val start = Deadline.now
    val (query, sort) = createQueryAndSort(m)
    val eventsEnumerator = getAggregateRootEvents(query, sort, m.traverse)
    val enumeratorWithCallBack = eventsEnumerator.onDoneEnumerating(() ⇒ {
      val lap = start.lap
      if (lap > readWarnThreshold)
        log.warning(s"""Fetching aggregate root events took longer than ${readWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
    })
    respondTo ! FetchedAggregateRootEvents(enumeratorWithCallBack)
  }

  private case object Initialize
  private case object Initialized
  private case class InitializeFailed(prob: Problem)

  def uninitializedReadWrite: Receive = {
    case Initialize ⇒
      log.info("Initializing(read/write)")
      val toTry = () => (for {
        collectinNames <- db.collectionNames
        createonRes <- if (collectinNames.contains(collectionName)) {
          log.info(s"""Collection "$collectionName" already exists.""")
          Future.successful(false)
        } else {
          log.info(s"""Collection "$collectionName" does not yet exist.""")
          val collection = db(collectionName)
          collection.indexesManager.ensure(MIndex(List("aggid" -> IndexType.Ascending, "version" -> IndexType.Ascending), name = Some("idx_aggid_version"), unique = false))
        }
      } yield createonRes).toAlmFuture

      context.retryWithLogging[Boolean](
        retryContext = s"Initialize collection $collectionName",
        toTry = toTry,
        onSuccess = createRes => {
          log.info(s"""Index on "aggid, version" created: $createRes""")
          self ! Initialized
        },
        onFinalFailure = (t, n, p) => {
          val prob = MandatoryDataProblem(s"Initialize collection '$collectionName' finally failed after $n attempts and ${t.defaultUnitString}.", cause = Some(p))
          self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob)))
        },
        log = this.log,
        settings = retrySettings,
        actorName = Some("initializes-collection"))

    case Initialized ⇒
      log.info("Initialized")
      registerCircuitControl(circuitBreaker)
      context.become(receiveAggregateRootEventLogMsg)

    case InitializeFailed(prob) ⇒
      log.error(s"Initialize failed:\n$prob")
      reportCriticalFailure(prob)
      sys.error(prob.message)

    case m: AggregateRootEventLogMessage ⇒
      val msg = s"""Received domain event log message ${m.getClass().getSimpleName()} while uninitialized."""
      log.warning(msg)
      val problem = ServiceNotAvailableProblem(msg)
      m match {
        case CommitAggregateRootEvent(event) ⇒
          sender ! AggregateRootEventNotCommitted(event.eventId, problem)
          reportMissedEvent(event, MajorSeverity, problem)
        case m: GetAllAggregateRootEvents ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEvent(eventId) ⇒
          sender ! GetAggregateRootEventFailed(eventId, problem)
        case m: GetAggregateRootEventsFor ⇒
          sender ! GetAggregateRootEventsFailed(problem)
      }
  }

  def uninitializedReadOnly: Receive = {
    case Initialize ⇒
      log.info("Initializing(read-only)")
      context.retryWithLogging[Unit](
        retryContext = s"Find collection $collectionName",
        toTry = () => db.collectionNames.toAlmFuture.foldV(
          fail => fail.failure,
          collectionNames => {
            if (collectionNames.contains(collectionName))
              ().success
            else
              MandatoryDataProblem(s"""Collection "$collectionName" is not among [${collectionNames.mkString(", ")}] in database "${db.name}".""").failure
          }),
        onSuccess = _ => { self ! Initialized },
        onFinalFailure = (t, n, p) => {
          val prob = MandatoryDataProblem(s"Look up collection '$collectionName' finally failed after $n attempts and ${t.defaultUnitString}.", cause = Some(p))
          self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob)))
        },
        log = this.log,
        settings = retrySettings,
        actorName = Some("looks-for-collection"))

    case Initialized ⇒
      log.info("Initialized")
      registerCircuitControl(circuitBreaker)
      context.become(receiveAggregateRootEventLogMsg)

    case InitializeFailed(prob) ⇒
      log.error(s"Initialize failed:\n$prob")
      reportCriticalFailure(prob)
      sys.error(prob.message)

    case m: AggregateRootEventLogMessage ⇒
      val msg = s"""Received domain event log message ${m.getClass().getSimpleName()} while uninitialized in read only mode."""
      log.warning(msg)
      val problem = ServiceNotAvailableProblem(msg)
      m match {
        case CommitAggregateRootEvent(event) ⇒
          sender ! AggregateRootEventNotCommitted(event.eventId, problem)
        case m: GetAllAggregateRootEvents ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEvent(eventId) ⇒
          sender ! GetAggregateRootEventFailed(eventId, problem)
        case m: GetAggregateRootEventsFor ⇒
          sender ! GetAggregateRootEventsFailed(problem)
      }
  }

  def receiveAggregateRootEventLogMsg: Receive = {
    case CommitAggregateRootEvent(event) ⇒
      if (readOnly) {
        val problem = IllegalOperationProblem("Read only mode is enabled.")
        sender() ! AggregateRootEventNotCommitted(event.eventId, problem)
        reportMinorFailure(problem)
      }
      commitEvent(event, sender())

    case m: AggregateRootEventLogQueryManyMessage ⇒
      fetchAndDispatchAggregateRootEvents(m, sender())

    case GetAggregateRootEvent(eventId) ⇒
      val collection = db(collectionName)
      val query = BSONDocument("_id" -> BSONString(eventId.value))
      (for {
        docs <- collection.find(query).cursor.collect[List](2, true).toAlmFuture
        aggregateRootEvent <- AlmFuture {
          docs match {
            case Nil ⇒ None.success
            case d :: Nil ⇒ documentToAggregateRootEvent(d).map(Some(_))
            case x ⇒ PersistenceProblem(s"""Expected 1 domain event with id "$eventId" but found ${x.size}.""").failure
          }
        }(serializationExecutor)
      } yield aggregateRootEvent).mapRecoverPipeTo(
        eventOpt ⇒ FetchedAggregateRootEvent(eventId, eventOpt),
        problem ⇒ {
          log.error(problem.toString())
          reportMajorFailure(problem)
          GetAggregateRootEventFailed(eventId, problem)
        })(sender())
  }

  override def receive =
    if (readOnly)
      uninitializedReadOnly
    else
      uninitializedReadWrite

  override def preStart() {
    super.preStart()
    self ! Initialize
  }

  override def postStop() {
    deregisterCircuitControl()
  }

}
