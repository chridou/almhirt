package almhirt.corex.mongo.eventlog

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.converters.BinaryConverter
import almhirt.configuration._
import almhirt.eventlog._
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index => MIndex }
import reactivemongo.api.indexes.IndexType
import play.api.libs.iteratee._

object MongoEventLog {
  def props(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeEvent: Event => AlmValidation[BSONDocument],
    deserializeEvent: BSONDocument => AlmValidation[Event],
    writeWarnThreshold: FiniteDuration)(implicit executionContexts: HasExecutionContexts): Props =
    Props(new MongoEventLogImpl(
      db,
      collectionName,
      serializeEvent,
      deserializeEvent,
      writeWarnThreshold))
}

private[almhirt] class MongoEventLogImpl(
  db: DB with DBMetaCommands,
  collectionName: String,
  serializeEvent: Event => AlmValidation[BSONDocument],
  deserializeEvent: BSONDocument => AlmValidation[Event],
  writeWarnThreshold: FiniteDuration)(implicit executionContexts: HasExecutionContexts) extends Actor with ActorLogging {
  import EventLog._
  import almhirt.corex.mongo.BsonConverter._

  implicit val defaultExecutor = executionContexts.futuresContext
  val serializationExecutor = executionContexts.futuresContext

  val projectionFilter = BSONDocument("event" -> 1)

  val noSorting = BSONDocument()
  val sortByTimestamp = BSONDocument("timestamp" -> 1)

  private case object Initialize
  private case object Initialized

  private val fromBsonDocToEvent: Enumeratee[BSONDocument, Event] =
    Enumeratee.mapM[BSONDocument] { doc => scala.concurrent.Future { documentToEvent(doc).resultOrEscalate }(serializationExecutor) }

  def eventToDocument(event: Event): AlmValidation[BSONDocument] = {
    (for {
      serialized <- serializeEvent(event)
    } yield {
      BSONDocument(
        ("_id" -> BSONString(event.eventId.value)),
        ("timestamp" -> localDateTimeToBsonDateTime(event.timestamp)),
        ("event" -> serialized))
    }).leftMap(p => SerializationProblem(s"""Could not serialize a "${event.getClass().getName()}".""", cause = Some(p)))
  }

  def documentToEvent(document: BSONDocument): AlmValidation[Event] = {
    document.get("event") match {
      case Some(d: BSONDocument) => deserializeEvent(d)
      case Some(x) => MappingProblem(s"""Payload must be contained as a BSONDocument. It is a "${x.getClass().getName()}".""").failure
      case None => NoSuchElementProblem("BSONDocument for payload not found").failure
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

  def storeEvent(event: Event): AlmFuture[Deadline] =
    for {
      serialized <- AlmFuture { eventToDocument(event: Event) }(serializationExecutor)
      start <- insertDocument(serialized)
    } yield start

  def commitEvent(event: Event) {
    storeEvent(event) onComplete (
      fail => log.error(fail.toString()),
      start => {
        val lap = start.lap
        if (lap > writeWarnThreshold)
          log.warning(s"""Storing event "${event.getClass().getSimpleName()}(${event.eventId})" took longer than ${writeWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
      })
  }

  def getEvents(query: BSONDocument, sort: BSONDocument): Enumerator[Event] = {
    val collection = db(collectionName)
    val enumerator = collection.find(query, projectionFilter).sort(sort).cursor.enumerate(10000, true)
    enumerator.through(fromBsonDocToEvent)
  }

  def fetchAndDispatchEvents(query: BSONDocument, sort: BSONDocument, respondTo: ActorRef) {
    respondTo ! FetchedEvents(getEvents(query, sort))
  }

  def uninitialized: Receive = {
    case Initialize =>
      log.info("Initializing")
      val collection = db(collectionName)
      (for {
        idxRes <- collection.indexesManager.ensure(MIndex(List("timestamp" -> IndexType.Ascending), name = Some("idx_timestamp"), unique = false))
      } yield (idxRes)).onComplete {
        case scala.util.Success(idxRes) =>
          log.info(s"""Index on "timestamp" created: $idxRes""")
          self ! Initialized
        case scala.util.Failure(exn) =>
          log.error(exn, "Failed to ensure indexes and/or collection capping")
          this.context.stop(self)
      }
    case Initialized =>
      log.info("Initialized")
      context.become(receiveEventLogMsg)
    case m: EventLogMessage =>
      log.warning(s"""Received event log message ${m.getClass().getSimpleName()} while uninitialized.""")
  }

  def receiveEventLogMsg: Receive = {
    case LogEvent(event) =>
      commitEvent(event)

    case FindEvent(eventId) =>
      val pinnedSender = sender
      val collection = db(collectionName)
      val query = BSONDocument("_id" -> BSONString(eventId.value))
      val res =
        for {
          docs <- collection.find(query).cursor.collect[List](2, true).toSuccessfulAlmFuture
          Event <- AlmFuture {
            docs match {
              case Nil => None.success
              case d :: Nil => documentToEvent(d).map(Some(_))
              case x => PersistenceProblem(s"""Expected 1 event with id "$eventId" but found ${x.size}.""").failure
            }
          }(serializationExecutor)
        } yield Event
      res.onComplete(
        problem => {
          pinnedSender ! FindEventFailed(eventId, problem)
          log.error(problem.toString())
        },
        eventOpt => pinnedSender ! FoundEvent(eventId, eventOpt))

    case FetchEventsFrom(from) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$gte" -> localDateTimeToBsonDateTime(from)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case FetchEventsAfter(after) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$gt" -> localDateTimeToBsonDateTime(after)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case FetchEventsTo(to) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$lte" -> localDateTimeToBsonDateTime(to)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case FetchEventsUntil(until) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$lt" -> localDateTimeToBsonDateTime(until)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case FetchEventsFromTo(from, to) =>
      val query = BSONDocument(
        "$and" -> BSONDocument(
          "timestamp" -> BSONDocument(
            "$gte" -> localDateTimeToBsonDateTime(from)),
          "timestamp" -> BSONDocument(
            "$lte" -> localDateTimeToBsonDateTime(to))))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case FetchEventsFromUntil(from, until) =>
      val query = BSONDocument(
        "$and" -> BSONDocument(
          "timestamp" -> BSONDocument(
            "$gte" -> localDateTimeToBsonDateTime(from)),
          "timestamp" -> BSONDocument(
            "$lt" -> localDateTimeToBsonDateTime(until))))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case FetchEventsAfterTo(after, to) =>
      val query = BSONDocument(
        "$and" -> BSONDocument(
          "timestamp" -> BSONDocument(
            "$gt" -> localDateTimeToBsonDateTime(after)),
          "timestamp" -> BSONDocument(
            "$lte" -> localDateTimeToBsonDateTime(to))))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case FetchEventsAfterUntil(after, until) =>
      val query = BSONDocument(
        "$and" -> BSONDocument(
          "timestamp" -> BSONDocument(
            "$gt" -> localDateTimeToBsonDateTime(after)),
          "timestamp" -> BSONDocument(
            "$lt" -> localDateTimeToBsonDateTime(until))))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)
  }

  override def receive = uninitialized

  override def preStart() {
    super.preStart()
    self ! Initialize
  }
}