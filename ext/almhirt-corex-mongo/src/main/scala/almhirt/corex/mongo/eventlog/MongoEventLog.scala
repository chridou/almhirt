package almhirt.corex.mongo.eventlog

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.converters.UuidConverter
import almhirt.configuration._
import almhirt.eventlog._
import almhirt.messaging.MessagePublisher
import almhirt.core.Almhirt
import almhirt.corex.mongo.LogStatisticsCollector
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index => MIndex }
import reactivemongo.api.indexes.IndexType
import com.typesafe.config.Config
import almhirt.eventlog.impl.DevNullEventLog

object MongoEventLog {
  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeEvent: Event => AlmValidation[BSONDocument],
    deserializeEvent: BSONDocument => AlmValidation[Event],
    writeWarnThreshold: FiniteDuration,
    serializationExecutor: ExecutionContext,
    statisticsCollector: Option[ActorRef],
    maxCollectionSize: Option[Long],
    theAlmhirt: Almhirt): Props =
    Props(new MongoEventLog(
      db,
      collectionName,
      serializeEvent,
      deserializeEvent,
      writeWarnThreshold,
      serializationExecutor,
      statisticsCollector,
      maxCollectionSize)(theAlmhirt))

  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeEvent: Event => AlmValidation[BSONDocument],
    deserializeEvent: BSONDocument => AlmValidation[Event],
    writeWarnThreshold: FiniteDuration,
    useNumberCruncherForSerialization: Boolean,
    statisticsCollector: Option[ActorRef],
    maxCollectionSize: Option[Long],
    theAlmhirt: Almhirt): Props = {
    val serCtx = if (useNumberCruncherForSerialization) theAlmhirt.numberCruncher else theAlmhirt.futuresExecutor
    propsRaw(
      db,
      collectionName,
      serializeEvent,
      deserializeEvent,
      writeWarnThreshold,
      serCtx,
      statisticsCollector,
      maxCollectionSize,
      theAlmhirt)
  }

  def props(db: DB with DBMetaCommands, serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], statisticsCollector: Option[ActorRef], configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      collectionName <- configSection.v[String]("table-name")
      writeWarnThreshold <- configSection.v[FiniteDuration]("write-warn-threshold-duration")
      useNumberCruncherForSerialization <- configSection.v[Boolean]("use-number-cruncher-for-serialization")
      capCollection <- configSection.v[Boolean]("cap-collection")
      maxCollectionSize <- if (capCollection)
        configSection.v[Long]("max-collection-size").map(Some(_))
      else
        None.success
    } yield {
      theAlmhirt.log.info(s"""MongoEventLog: table-name = $collectionName""")
      theAlmhirt.log.info(s"""MongoEventLog: write-warn-threshold-duration = ${writeWarnThreshold.defaultUnitString}""")
      theAlmhirt.log.info(s"""MongoEventLog: use-number-cruncher-for-serialization = $useNumberCruncherForSerialization""")
      val maxSize = maxCollectionSize.map(_.toString).getOrElse("unlimited")
      theAlmhirt.log.info(s"""MongoEventLog: max-collection-size = $maxSize""")
      propsRaw(db, collectionName, serializeEvent, deserializeEvent, writeWarnThreshold, useNumberCruncherForSerialization, statisticsCollector, maxCollectionSize, theAlmhirt)
    }

  def props(driver: MongoDriver, serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], statisticsCollector: Option[ActorRef], configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      connections <- configSection.v[List[String]]("connections")
      dbName <- configSection.v[String]("db-name")
      db <- inTryCatch {
        val connection = driver.connection(connections)
        connection(dbName)(theAlmhirt.futuresExecutor)
      }
      props <- props(db, serializeEvent, deserializeEvent, statisticsCollector, configSection, theAlmhirt)
    } yield {
      theAlmhirt.log.info(s"""MongoEventLog: connections = ${connections.mkString("[", ",", "]")}""")
      theAlmhirt.log.info(s"""MongoEventLog: db-name = $dbName""")
      props
    }

  def props(driver: MongoDriver, serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], statisticsCollector: Option[ActorRef], configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap { configSection =>
      props(driver, serializeEvent, deserializeEvent, statisticsCollector, configSection, theAlmhirt)
    }

  def props(driver: MongoDriver, serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], statisticsCollector: Option[ActorRef], theAlmhirt: Almhirt): AlmValidation[Props] =
    props(driver, serializeEvent, deserializeEvent, statisticsCollector, "almhirt.event-log", theAlmhirt)

  def props(serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], configPath: String, statisticsCollector: Option[ActorRef], theAlmhirt: Almhirt): AlmValidation[Props] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      props(driver, serializeEvent, deserializeEvent, statisticsCollector, configPath, theAlmhirt))

  def props(serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], statisticsCollector: Option[ActorRef], theAlmhirt: Almhirt): AlmValidation[Props] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      props(driver, serializeEvent, deserializeEvent, statisticsCollector, theAlmhirt))

  def apply(driver: MongoDriver, serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], configPath: String, theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      collectStatistics <- configSection.v[Boolean]("collect-statistics")
      enabled <- configSection.v[Boolean]("enabled")
      props <- {
        theAlmhirt.log.info(s"""MongoEventLog: collect-statistics = $collectStatistics""")
        val collector =
          if (collectStatistics)
            Some(theAlmhirt.actorSystem.actorOf(Props[LogStatisticsCollector], "event-log-statistics-collector"))
          else
            None
        props(driver, serializeEvent, deserializeEvent, collector, configSection, theAlmhirt)
      }
    } yield {
      if (!enabled)
        theAlmhirt.log.info(s"""MongoEventLog: THE EVENT LOG IS DISABLED""")
      val actor = 
        if(enabled)
        	theAlmhirt.actorSystem.actorOf(props, "event-log")
        else
           theAlmhirt.actorSystem.actorOf(Props(new DevNullEventLog), "event-log")
      (actor, CloseHandle.noop)
    }

  def apply(serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], configPath: String, theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      apply(driver, serializeEvent, deserializeEvent, configPath, theAlmhirt))

  def apply(driver: MongoDriver, serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    apply(driver, serializeEvent, deserializeEvent, "almhirt.event-log", theAlmhirt)

  def apply(serializeEvent: Event => AlmValidation[BSONDocument], deserializeEvent: BSONDocument => AlmValidation[Event], theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    apply(serializeEvent, deserializeEvent, "almhirt.event-log", theAlmhirt)
}

class MongoEventLog(
  db: DB with DBMetaCommands,
  collectionName: String,
  serializeEvent: Event => AlmValidation[BSONDocument],
  deserializeEvent: BSONDocument => AlmValidation[Event],
  writeWarnThreshold: FiniteDuration,
  serializationExecutor: ExecutionContext,
  statisticsCollector: Option[ActorRef],
  maxCollectionSize: Option[Long])(implicit theAlmhirt: Almhirt) extends Actor with ActorLogging with EventLog {
  import EventLog._
  import almhirt.corex.mongo.LogStatisticsCollector._
  import almhirt.corex.mongo.BsonConverter._

  implicit val defaultExecutor = theAlmhirt.futuresExecutor

  val projectionFilter = BSONDocument("event" -> 1)

  val noSorting = BSONDocument()
  val sortByTimestamp = BSONDocument("timestamp" -> 1)

  private case object Initialize
  private case object Initialized

  def EventToDocument(event: Event): AlmValidation[BSONDocument] = {
    (for {
      serialized <- {
        val start = Deadline.now
        val res = serializeEvent(event)
        statisticsCollector.foreach(_ ! AddSerializationDuration(start.lap))
        res
      }
    } yield {
      BSONDocument(
        ("_id" -> uuidToBson(event.eventId)),
        ("timestamp" -> localDateTimeToBsonDateTime(event.timestamp)),
        ("event" -> serialized))
    }).leftMap(p => SerializationProblem(s"""Could not serialize a "${event.getClass().getName()}".""", cause = Some(p)))
  }

  def documentToEvent(document: BSONDocument): AlmValidation[Event] = {
    document.get("event") match {
      case Some(d: BSONDocument) =>
        val start = Deadline.now
        val res = deserializeEvent(d)
        statisticsCollector.foreach(_ ! AddDeserializationDuration(start.lap))
        res
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
      serialized <- AlmFuture { EventToDocument(event: Event) }(serializationExecutor)
      start <- insertDocument(serialized)
    } yield start

  def commitEvent(event: Event) {
    storeEvent(event) onComplete (
      fail => log.error(fail.toString()),
      start => {
        val lap = start.lap
        statisticsCollector.foreach(_ ! AddWriteDuration(lap))
        if (lap > writeWarnThreshold)
          log.warning(s"""Storing event "${event.getClass().getSimpleName()}(${event.eventId})" took longer than ${writeWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
      })
  }

  def getEvents(query: BSONDocument, sort: BSONDocument): AlmFuture[Seq[Event]] = {
    val collection = db(collectionName)
    for {
      docs <- collection.find(query, projectionFilter).sort(sort).cursor.toList.toSuccessfulAlmFuture
      Events <- AlmFuture {
        docs.map(x => documentToEvent(x).toAgg).sequence
      }(serializationExecutor)
    } yield Events
  }

  def fetchAndDispatchEvents(query: BSONDocument, sort: BSONDocument, respondTo: ActorRef) {
    val start = Deadline.now
    getEvents(query, sort).fold(
      problem => {
        log.error(problem.toString())
      },
      events => {
        respondTo ! FetchedEventsBatch(events)
        statisticsCollector.foreach(_ ! AddReadDuration(start.lap))
      })
  }

  def uninitialized: Receive = {
    case Initialize =>
      log.info("Initializing")
      val collection = db(collectionName)
      (for {
        capRes <- maxCollectionSize match {
          case Some(maxSize) => collection.convertToCapped(maxSize, None)
          case None => scala.concurrent.Promise.successful(false).future
        }
        idxRes <- collection.indexesManager.ensure(MIndex(List("timestamp" -> IndexType.Ascending), name = Some("idx_timestamp"), unique = false))
      } yield (capRes, idxRes)).onComplete {
        case scala.util.Success((capRes, idxRes)) =>
          log.info(s"""Index on "timestamp" created: $idxRes""")
          log.info(s"""Collection capped: $capRes""")
          self ! Initialized
        case scala.util.Failure(exn) =>
          log.error(exn, "Failed to ensure indexes and/or collection capping,")
          this.context.stop(self)
      }
    case Initialized =>
      log.info("Initialized")
      context.become(receiveEventLogMsg)
    case m: EventLogMessage =>
      log.warning(s"""Received event log message ${m.getClass().getSimpleName()} while uninitialized.""")
  }

  override def receiveEventLogMsg: Receive = {
    case LogEvent(event) =>
      commitEvent(event)

    case GetAllEvents =>
      fetchAndDispatchEvents(BSONDocument(), sortByTimestamp, sender)

    case GetEvent(eventId) =>
      val pinnedSender = sender
      val collection = db(collectionName)
      val query = BSONDocument("_id" -> BSONBinary(UuidConverter.uuidToBytes(eventId), Subtype.UuidSubtype))
      val res =
        for {
          docs <- collection.find(query).cursor.toList.toSuccessfulAlmFuture
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
          pinnedSender ! EventQueryFailed(eventId, problem)
          log.error(problem.toString())
        },
        eventOpt => pinnedSender ! QueriedEvent(eventId, eventOpt))

    case GetEventsFrom(from) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$gte" -> localDateTimeToBsonDateTime(from)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case GetEventsAfter(after) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$gt" -> localDateTimeToBsonDateTime(after)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case GetEventsTo(to) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$lte" -> localDateTimeToBsonDateTime(to)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case GetEventsUntil(until) =>
      val query = BSONDocument(
        "timestamp" -> BSONDocument("$lt" -> localDateTimeToBsonDateTime(until)))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case GetEventsFromTo(from, to) =>
      val query = BSONDocument(
        "$and" -> BSONDocument(
          "timestamp" -> BSONDocument(
            "$gte" -> localDateTimeToBsonDateTime(from)),
          "timestamp" -> BSONDocument(
            "$lte" -> localDateTimeToBsonDateTime(to))))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case GetEventsFromUntil(from, until) =>
      val query = BSONDocument(
        "$and" -> BSONDocument(
          "timestamp" -> BSONDocument(
            "$gte" -> localDateTimeToBsonDateTime(from)),
          "timestamp" -> BSONDocument(
            "$lt" -> localDateTimeToBsonDateTime(until))))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case GetEventsAfterTo(after, to) =>
      val query = BSONDocument(
        "$and" -> BSONDocument(
          "timestamp" -> BSONDocument(
            "$gt" -> localDateTimeToBsonDateTime(after)),
          "timestamp" -> BSONDocument(
            "$lte" -> localDateTimeToBsonDateTime(to))))
      fetchAndDispatchEvents(query, sortByTimestamp, sender)

    case GetEventsAfterUntil(after, until) =>
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