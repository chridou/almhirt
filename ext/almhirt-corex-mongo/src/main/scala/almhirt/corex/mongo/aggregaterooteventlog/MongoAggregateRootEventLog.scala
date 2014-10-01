package almhirt.corex.mongo.aggregaterooteventlog

import scala.language.reflectiveCalls
import scala.concurrent._
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.converters.BinaryConverter
import almhirt.configuration._
import almhirt.context.AlmhirtContext
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
    readOnlySettings: Option[RetrySettings])(implicit executionContexts: HasExecutionContexts): Props =
    Props(new MongoAggregateRootEventLogImpl(
      db,
      collectionName,
      serializeAggregateRootEvent,
      deserializeAggregateRootEvent,
      writeWarnThreshold,
      readWarnThreshold,
      readOnlySettings))

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
      readOnly <- section.v[Boolean]("read-only")
      readOnlySettings <- if (readOnly) {
        section.v[RetrySettings]("read-only-collection-lookup-retries").map(Some(_))
      } else {
        None.success
      }
    } yield propsRaw(
      db,
      collectionName,
      serializeAggregateRootEvent,
      deserializeAggregateRootEvent,
      writeWarnThreshold,
      readWarnThreshold,
      readOnlySettings)
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
  readOnlySettings: Option[RetrySettings])(implicit executionContexts: HasExecutionContexts) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
  import almhirt.eventlog.AggregateRootEventLog._

  implicit val defaultExecutor = executionContexts.futuresContext
  val serializationExecutor = executionContexts.futuresContext

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
        ("event" -> serialized))
    }).leftMap(p ⇒ SerializationProblem(s"""Could not serialize a "${aggregateRootEvent.getClass().getName()}".""", cause = Some(p)))
  }

  def documentToAggregateRootEvent(document: BSONDocument): AlmValidation[AggregateRootEvent] = {
    (document.get("domainevent") match {
      case Some(d: BSONDocument) ⇒
        deserializeAggregateRootEvent(d)
      case Some(x) ⇒
        val msg = s"""Payload must be contained as a BSONDocument. It is a "${x.getClass().getName()}"."""
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
    storeEvent(event) onComplete (
      fail ⇒ {
        log.error(s"Could not commit aggregate root event:\n$fail")
        respondTo ! AggregateRootEventNotCommitted(event.eventId, fail)
      },
      start ⇒ {
        respondTo ! AggregateRootEventCommitted(event.eventId)
        val lap = start.lap
        if (lap > writeWarnThreshold)
          log.warning(s"""Storing aggregate root event "${event.getClass().getSimpleName()}(${event.eventId})" took longer than ${writeWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
      })
  }

  def getAggregateRootEventsDocs(query: BSONDocument, sort: BSONDocument): Enumerator[BSONDocument] = {
    val collection = db(collectionName)
    val enumerator = collection.find(query, projectionFilter).sort(sort).cursor.enumerate(10000, true)
    enumerator
  }

  def getAggregateRootEvents(query: BSONDocument, sort: BSONDocument): Enumerator[AggregateRootEvent] = {
    val docsEnumerator = getAggregateRootEventsDocs(query, sort)
    docsEnumerator.through(fromBsonDocToAggregateRootEvent)
  }

  def fetchAndDispatchAggregateRootEvents(query: BSONDocument, sort: BSONDocument, respondTo: ActorRef) {
    val start = Deadline.now
    val eventsEnumerator = getAggregateRootEvents(query, sort)
    val enumeratorWithCallBack = eventsEnumerator.onDoneEnumerating(() ⇒ {
      val lap = start.lap
      if (lap > readWarnThreshold)
        log.warning(s"""Fetching domain events took longer than ${readWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
    })
    respondTo ! FetchedAggregateRootEvents(enumeratorWithCallBack)
  }

  private case object Initialize
  private case object Initialized
  private case class InitializeFailed(prob: Problem)

  def uninitializedReadWrite: Receive = {
    case Initialize ⇒
      log.info("Initializing")
      (for {
        collectinNames <- db.collectionNames
        createonRes <- if (collectinNames.contains(collectionName)) {
          log.info(s"""Collection "$collectionName" already exists.""")
          Future.successful(false)
        } else {
          log.info(s"""Collection "$collectionName" does not yet exist.""")
          val collection = db(collectionName)
          collection.indexesManager.ensure(MIndex(List("aggid" -> IndexType.Ascending, "version" -> IndexType.Ascending), name = Some("idx_aggid_version"), unique = false))
        }
      } yield createonRes).toAlmFuture.onComplete(
        problem ⇒
          self ! InitializeFailed(problem),
        createonRes ⇒ {
          log.info(s"""Index on "aggid, version" created: $createonRes""")
          self ! Initialized
        })

    case Initialized ⇒
      log.info("Initialized")
      context.become(receiveAggregateRootEventLogMsg(false))

    case InitializeFailed(prob) ⇒
      log.error(s"Initialize failed:\n$prob")
      sys.error(prob.message)

    case m: AggregateRootEventLogMessage ⇒
      log.warning(s"""Received domain event log message ${m.getClass().getSimpleName()} while uninitialized.""")
      val problem = PersistenceProblem("The event log is not yet initialized")
      m match {
        case CommitAggregateRootEvent(event) ⇒
          sender ! AggregateRootEventNotCommitted(event.eventId, problem)
        case GetAllAggregateRootEvents ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEvent(eventId) ⇒
          sender ! GetAggregateRootEventFailed(eventId, problem)
        case GetAllAggregateRootEventsFor(aggId) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsFrom(aggId, fromVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsTo(aggId, toVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsUntil(aggId, untilVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsFromTo(aggId, fromVersion, toVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsFromUntil(aggId, fromVersion, untilVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
      }
  }

  def uninitializedReadOnly(collectionLookupRetries: RetrySettings): Receive = {
    case Initialize ⇒
      log.info("Initializing(read-only)")
      context.retry[Unit](
        () => db.collectionNames.toAlmFuture.foldV(
          fail => fail.failure,
          collectionNames => {
            if (collectionNames.contains(collectionName))
              ().success
            else
              MandatoryDataProblem(s"""Collection "$collectionName" is not among [${collectionNames.mkString(", ")}] in database "${db.name}".""").failure
          }),
        _ => { self ! Initialized },
        (t, n, p) => log.info(s"Look up collection '$collectionName' failed after $n attempts and ${t.defaultUnitString}:\n$p"),
        (t, n, p) => {
          val prob = MandatoryDataProblem(s"Look up collection '$collectionName' finally failed after $n attempts and ${t.defaultUnitString}:\n$p")
          self ! InitializeFailed(UnspecifiedProblem(s""))
        },
        collectionLookupRetries,
        Some("looks-for-collection"))

    case Initialized ⇒
      log.info("Initialized")
      context.become(receiveAggregateRootEventLogMsg(true))

    case InitializeFailed(prob) ⇒
      log.error(s"Initialize failed:\n$prob")
      sys.error(prob.message)

    case m: AggregateRootEventLogMessage ⇒
      log.warning(s"""Received domain event log message ${m.getClass().getSimpleName()} while uninitialized.""")
      val problem = PersistenceProblem("The event log is not yet initialized")
      m match {
        case CommitAggregateRootEvent(event) ⇒
          sender ! AggregateRootEventNotCommitted(event.eventId, problem)
        case GetAllAggregateRootEvents ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEvent(eventId) ⇒
          sender ! GetAggregateRootEventFailed(eventId, problem)
        case GetAllAggregateRootEventsFor(aggId) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsFrom(aggId, fromVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsTo(aggId, toVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsUntil(aggId, untilVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsFromTo(aggId, fromVersion, toVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
        case GetAggregateRootEventsFromUntil(aggId, fromVersion, untilVersion) ⇒
          sender ! GetAggregateRootEventsFailed(problem)
      }
  }

  def receiveAggregateRootEventLogMsg(readOnly: Boolean): Receive = {
    case CommitAggregateRootEvent(event) ⇒
      if (readOnly)
        sender() ! AggregateRootEventNotCommitted(event.eventId, IllegalOperationProblem("Read only mode is enabled."))
      commitEvent(event, sender())

    case GetAllAggregateRootEvents ⇒
      fetchAndDispatchAggregateRootEvents(BSONDocument(), noSorting, sender)

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
          GetAggregateRootEventFailed(eventId, problem)
        })(sender())

    case GetAllAggregateRootEventsFor(aggId) ⇒
      val query = BSONDocument("aggid" -> BSONString(aggId.value))
      fetchAndDispatchAggregateRootEvents(query, sortByVersion, sender)

    case GetAggregateRootEventsFrom(aggId, fromVersion) ⇒
      val query = BSONDocument(
        "aggid" -> BSONString(aggId.value),
        "version" -> BSONDocument("$gte" -> BSONLong(fromVersion.value)))
      fetchAndDispatchAggregateRootEvents(query, sortByVersion, sender)

    case GetAggregateRootEventsTo(aggId, toVersion) ⇒
      val query = BSONDocument(
        "aggid" -> BSONString(aggId.value),
        "version" -> BSONDocument("$lte" -> BSONLong(toVersion.value)))
      fetchAndDispatchAggregateRootEvents(query, sortByVersion, sender)

    case GetAggregateRootEventsUntil(aggId, untilVersion) ⇒
      val query = BSONDocument(
        "aggid" -> BSONString(aggId.value),
        "version" -> BSONDocument("$lt" -> BSONLong(untilVersion.value)))
      fetchAndDispatchAggregateRootEvents(query, sortByVersion, sender)

    case GetAggregateRootEventsFromTo(aggId, fromVersion, toVersion) ⇒
      val query = BSONDocument(
        "aggid" -> BSONString(aggId.value),
        "$and" -> BSONArray(
          BSONDocument("version" -> BSONDocument("$gte" -> BSONLong(fromVersion.value))),
          BSONDocument("version" -> BSONDocument("$lte" -> BSONLong(toVersion.value)))))
      fetchAndDispatchAggregateRootEvents(query, sortByVersion, sender)

    case GetAggregateRootEventsFromUntil(aggId, fromVersion, untilVersion) ⇒
      val query = BSONDocument(
        "aggid" -> BSONString(aggId.value),
        "$and" -> BSONArray(
          BSONDocument("version" -> BSONDocument("$gte" -> BSONLong(fromVersion.value))),
          BSONDocument("version" -> BSONDocument("$lt" -> BSONLong(untilVersion.value)))))
      fetchAndDispatchAggregateRootEvents(query, sortByVersion, sender)
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
}
