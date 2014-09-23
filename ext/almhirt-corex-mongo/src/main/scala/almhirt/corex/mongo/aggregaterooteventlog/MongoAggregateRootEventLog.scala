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
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType
import reactivemongo.core.commands.GetLastError
import com.typesafe.config.Config
import play.api.libs.iteratee._

object MongoAggregateRootEventLog {
  def props(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    writeWarnThreshold: FiniteDuration,
    readWarnThreshold: FiniteDuration)(implicit executionContexts: HasExecutionContexts): Props =
    Props(new MongoAggregateRootEventLogImpl(
      db,
      collectionName,
      serializeAggregateRootEvent,
      deserializeAggregateRootEvent,
      writeWarnThreshold,
      readWarnThreshold))
}

private[almhirt] class MongoAggregateRootEventLogImpl(
  db: DB with DBMetaCommands,
  collectionName: String,
  serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
  deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
  writeWarnThreshold: FiniteDuration,
  readWarnThreshold: FiniteDuration)(implicit executionContexts: HasExecutionContexts) extends Actor with ActorLogging {
  import almhirt.eventlog.AggregateRootEventLog._

  implicit val defaultExecutor = executionContexts.futuresContext
  val serializationExecutor = executionContexts.futuresContext

  val projectionFilter = BSONDocument("event" -> 1)

  val noSorting = BSONDocument()
  val sortByVersion = BSONDocument("aggid" -> 1, "version" -> 1)

  private case object Initialize
  private case object Initialized

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
      lastError <- collection.insert(document).toSuccessfulAlmFuture
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

  def uninitialized: Receive = {
    case Initialize ⇒
      log.info("Initializing")
      (for {
        collectinNames <- db.collectionNames
        createonRes <- if (collectinNames.contains(collectionName)) {
          log.info(s"""Collection "$collectionName" already exists.""")
          Future.successful(db(collectionName))
        } else {
          log.info(s"""Collection "$collectionName" does not yet exist.""")
          val collection = db(collectionName)
          collection.indexesManager.ensure(MIndex(List("aggid" -> IndexType.Ascending, "version" -> IndexType.Ascending), name = Some("idx_aggid_version"), unique = false))
        }
      } yield createonRes).onComplete {
        case scala.util.Success(a) ⇒
          log.info(s"""Index on "aggid, version" created: $a""")
          self ! Initialized
        case scala.util.Failure(exn) ⇒
          log.error(exn, "Failed to ensure indexes.")
          this.context.stop(self)
      }

    case Initialized ⇒
      log.info("Initialized")
      context.become(receiveAggregateRootEventLogMsg)

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

  def receiveAggregateRootEventLogMsg: Receive = {
    case CommitAggregateRootEvent(event) ⇒
      commitEvent(event, sender())

    case GetAllAggregateRootEvents ⇒
      fetchAndDispatchAggregateRootEvents(BSONDocument(), noSorting, sender)

    case GetAggregateRootEvent(eventId) ⇒
      val pinnedSender = sender
      val collection = db(collectionName)
      val query = BSONDocument("_id" -> BSONString(eventId.value))
      val res =
        for {
          docs <- collection.find(query).cursor.collect[List](2, true).toSuccessfulAlmFuture
          aggregateRootEvent <- AlmFuture {
            docs match {
              case Nil ⇒ None.success
              case d :: Nil ⇒ documentToAggregateRootEvent(d).map(Some(_))
              case x ⇒ PersistenceProblem(s"""Expected 1 domain event with id "$eventId" but found ${x.size}.""").failure
            }
          }(serializationExecutor)
        } yield aggregateRootEvent
      res.onComplete(
        problem ⇒ {
          pinnedSender ! GetAggregateRootEventFailed(eventId, problem)
          log.error(problem.toString())
        },
        eventOpt ⇒ pinnedSender ! FetchedAggregateRootEvent(eventId, eventOpt))

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

  override def receive = uninitialized

  override def preStart() {
    super.preStart()
    self ! Initialize
  }
}