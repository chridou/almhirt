package almhirt.corex.mongo.domaineventlog

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.util.UuidConverter
import almhirt.configuration._
import almhirt.domaineventlog._
import almhirt.messaging.MessagePublisher
import almhirt.domain.DomainEvent
import almhirt.core.Almhirt
import almhirt.corex.mongo.LogStatisticsCollector
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index => MIndex }
import reactivemongo.api.indexes.IndexType
import com.typesafe.config.Config

object MongoDomainEventLog {
  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument],
    deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent],
    writeWarnThreshold: FiniteDuration,
    readWarnThreshold: FiniteDuration,
    serializationExecutor: ExecutionContext,
    statisticsCollector: Option[ActorRef],
    maxCollectionSize: Option[Long],
    theAlmhirt: Almhirt): Props =
    Props(new MongoDomainEventLog(
      db,
      collectionName,
      serializeDomainEvent,
      deserializeDomainEvent,
      writeWarnThreshold,
      readWarnThreshold,
      serializationExecutor,
      statisticsCollector,
      maxCollectionSize)(theAlmhirt))

  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument],
    deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent],
    writeWarnThreshold: FiniteDuration,
    readWarnThreshold: FiniteDuration,
    useNumberCruncherForSerialization: Boolean,
    statisticsCollector: Option[ActorRef],
    maxCollectionSize: Option[Long],
    theAlmhirt: Almhirt): Props = {
    val serCtx = if (useNumberCruncherForSerialization) theAlmhirt.numberCruncher else theAlmhirt.futuresExecutor
    propsRaw(
      db,
      collectionName,
      serializeDomainEvent,
      deserializeDomainEvent,
      writeWarnThreshold,
      readWarnThreshold,
      serCtx,
      statisticsCollector,
      maxCollectionSize,
      theAlmhirt)
  }

  def props(db: DB with DBMetaCommands, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], statisticsCollector: Option[ActorRef], configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      collectionName <- configSection.v[String]("table-name")
      writeWarnThreshold <- configSection.v[FiniteDuration]("write-warn-threshold-duration")
      readWarnThreshold <- configSection.v[FiniteDuration]("read-warn-threshold-duration")
      useNumberCruncherForSerialization <- configSection.v[Boolean]("use-number-cruncher-for-serialization")
      capCollection <- configSection.v[Boolean]("cap-collection")
      maxCollectionSize <- if (capCollection)
        configSection.v[Long]("max-collection-size").map(Some(_))
      else
        None.success
    } yield {
      theAlmhirt.log.info(s"""MongoDomainEventLog: table-name = $collectionName""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: write-warn-threshold-duration = ${writeWarnThreshold.defaultUnitString}""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: read-warn-threshold-duration = ${readWarnThreshold.defaultUnitString}""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: use-number-cruncher-for-serialization = $useNumberCruncherForSerialization""")
      val maxSize = maxCollectionSize.map(_.toString).getOrElse("unlimited")
      theAlmhirt.log.info(s"""MongoDomainEventLog: max-collection-size = $maxSize""")
      propsRaw(db, collectionName, serializeDomainEvent, deserializeDomainEvent, writeWarnThreshold, readWarnThreshold, useNumberCruncherForSerialization, statisticsCollector, maxCollectionSize, theAlmhirt)
    }

  def props(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], statisticsCollector: Option[ActorRef], configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      connections <- configSection.v[List[String]]("connections")
      dbName <- configSection.v[String]("db-name")
      db <- inTryCatch {
        val connection = driver.connection(connections)
        connection(dbName)(theAlmhirt.futuresExecutor)
      }
      props <- props(db, serializeDomainEvent, deserializeDomainEvent, statisticsCollector, configSection, theAlmhirt)
    } yield {
      theAlmhirt.log.info(s"""MongoDomainEventLog: connections = ${connections.mkString("[", ",", "]")}""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: db-name = $dbName""")
      props
    }

  def props(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], statisticsCollector: Option[ActorRef], configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap { configSection =>
      props(driver, serializeDomainEvent, deserializeDomainEvent, statisticsCollector, configSection, theAlmhirt)
    }

  def props(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], statisticsCollector: Option[ActorRef], theAlmhirt: Almhirt): AlmValidation[Props] =
    props(driver, serializeDomainEvent, deserializeDomainEvent, statisticsCollector, "almhirt.domain-event-log", theAlmhirt)

  def props(serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], configPath: String, statisticsCollector: Option[ActorRef], theAlmhirt: Almhirt): AlmValidation[Props] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      props(driver, serializeDomainEvent, deserializeDomainEvent, statisticsCollector, configPath, theAlmhirt))

  def props(serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], statisticsCollector: Option[ActorRef], theAlmhirt: Almhirt): AlmValidation[Props] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      props(driver, serializeDomainEvent, deserializeDomainEvent, statisticsCollector, theAlmhirt))

  def apply(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], configPath: String, theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      collectStatistics <- configSection.v[Boolean]("collect-statistics")
      props <- {
        theAlmhirt.log.info(s"""MongoDomainEventLog: collect-statistics = $collectStatistics""")
        val collector =
          if (collectStatistics)
            Some(theAlmhirt.actorSystem.actorOf(Props[LogStatisticsCollector], "domain-event-log-statistics-collector"))
          else
            None
        props(driver, serializeDomainEvent, deserializeDomainEvent, collector, configSection, theAlmhirt)
      }
    } yield {
      val actor = theAlmhirt.actorSystem.actorOf(props, "domain-event-log")
      (actor, CloseHandle.noop)
    }

  def apply(serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], configPath: String, theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      apply(driver, serializeDomainEvent, deserializeDomainEvent, configPath, theAlmhirt))

  def apply(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    apply(driver, serializeDomainEvent, deserializeDomainEvent, "almhirt.domain-event-log", theAlmhirt)

  def apply(serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    apply(serializeDomainEvent, deserializeDomainEvent, "almhirt.domain-event-log", theAlmhirt)
}

class MongoDomainEventLog(
  db: DB with DBMetaCommands,
  collectionName: String,
  serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument],
  deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent],
  writeWarnThreshold: FiniteDuration,
  readWarnThreshold: FiniteDuration,
  serializationExecutor: ExecutionContext,
  statisticsCollector: Option[ActorRef],
  maxCollectionSize: Option[Long])(implicit theAlmhirt: Almhirt) extends Actor with ActorLogging with DomainEventLog {
  import DomainEventLog._
  import almhirt.corex.mongo.LogStatisticsCollector._
  import almhirt.corex.mongo.BsonConverter._

  implicit val defaultExecutor = theAlmhirt.futuresExecutor

  override def publishCommittedEvent(domainEvent: DomainEvent) = theAlmhirt.messageBus.publish(domainEvent)

  val projectionFilter = BSONDocument("domainevent" -> 1)

  val noSorting = BSONDocument()
  val sortByVersion = BSONDocument("version" -> 1)

  private case object Initialize
  private case object Initialized

  def domainEventToDocument(domainEvent: DomainEvent): AlmValidation[BSONDocument] = {
    for {
      serialized <- {
        val start = Deadline.now
        val res = serializeDomainEvent(domainEvent)
        statisticsCollector.foreach(_ ! AddSerializationDuration(start.lap))
        res
      }
    } yield {
      BSONDocument(
        ("_id" -> uuidToBson(domainEvent.eventId)),
        ("aggid" -> uuidToBson(domainEvent.aggId)),
        ("version" -> BSONLong(domainEvent.aggVersion)),
        ("domainevent" -> serialized))
    }
  }

  def documentToDomainEvent(document: BSONDocument): AlmValidation[DomainEvent] = {
    document.get("domainevent") match {
      case Some(d: BSONDocument) =>
        val start = Deadline.now
        val res = deserializeDomainEvent(d)
        statisticsCollector.foreach(_ ! AddDeserializationDuration(start.lap))
        res
      case Some(x) => MappingProblem(s"""Payload must be contained as a BSONDocument. It is a "${x.getClass().getName()}".""").failure
      case None => NoSuchElementProblem("BSONDocument for payload not found").failure
    }
  }

  def insertDocument(document: BSONDocument): AlmFuture[Unit] = {
    val collection = db(collectionName)
    for {
      lastError <- collection.insert(document).toSuccessfulAlmFuture
      _ <- if (lastError.ok)
        AlmFuture.successful(())
      else {
        val msg = lastError.errMsg.getOrElse("unknown error")
        AlmFuture.failed(PersistenceProblem(msg))
      }
    } yield ()
  }

  def storeDomainEvent(domainEvent: DomainEvent): AlmFuture[DomainEvent] =
    for {
      serialized <- AlmFuture { domainEventToDocument(domainEvent: DomainEvent) }(serializationExecutor)
      _ <- insertDocument(serialized)
    } yield domainEvent

  def commitDomainEvent(domainEvent: DomainEvent): AlmFuture[DomainEvent] = {
    storeDomainEvent(domainEvent) andThen (
      fail => log.error(fail.toString()),
      succ => publishCommittedEvent(succ))
  }

  def getEvents(query: BSONDocument, sort: BSONDocument): AlmFuture[Seq[DomainEvent]] = {
    val collection = db(collectionName)
    for {
      docs <- collection.find(query, projectionFilter).sort(sort).cursor.toList.toSuccessfulAlmFuture
      domainEvents <- AlmFuture {
        docs.map(x => documentToDomainEvent(x).toAgg).sequence
      }(serializationExecutor)
    } yield domainEvents
  }

  def fetchAndDispatchDomainEvents(query: BSONDocument, sort: BSONDocument, respondTo: ActorRef) {
    val start = Deadline.now
    getEvents(query, sort).fold(
      problem => {
        sender ! FetchedDomainEventsFailure(problem)
        log.error(problem.toString())
      },
      domainEvents => {
        val lap = start.lap
        respondTo ! FetchedDomainEventsBatch(domainEvents)
        if (lap > readWarnThreshold)
          log.warning(s"""Storing ${domainEvents.size} domain events took longer than ${readWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
        statisticsCollector.foreach(_ ! AddReadDuration(lap))
      })
  }

  def uninitialized: Receive = {
    case Initialize =>
      log.info("Initializing")
      val collection = db(collectionName)
      val indexesRes =
        for {
          capRes <- maxCollectionSize match {
            case Some(maxSize) => collection.convertToCapped(maxSize, None)
            case None => scala.concurrent.Promise.successful(false).future
          }
          a <- collection.indexesManager.ensure(MIndex(List("aggid" -> IndexType.Ascending), unique = false))
          b <- collection.indexesManager.ensure(MIndex(List("aggid" -> IndexType.Ascending, "version" -> IndexType.Ascending), unique = false))
        } yield (a, b, capRes)
      indexesRes.onComplete {
        case scala.util.Success((a, b, capRes)) =>
          log.info(s"""Index on "aggid" created: $a""")
          log.info(s"""Index on "aggid, version" created: $b""")
          log.info(s"""Collection capped: $capRes""")
          self ! Initialized
        case scala.util.Failure(exn) =>
          log.error(exn, "Failed to ensure indexes.")
          this.context.stop(self)
      }
    case Initialized =>
      log.info("Initialized")
      context.become(receiveDomainEventLogMsg)
    case m: DomainEventLogMessage =>
      log.warning(s"""Received domain event log message ${m.getClass().getSimpleName()} while uninitialized.""")
  }

  override def receiveDomainEventLogMsg: Receive = {
    case CommitDomainEvents(events) =>
      val pinnedSender = sender
      val start = Deadline.now
      val committedEvents = AlmFuture.sequence(events.map(commitDomainEvent))
      committedEvents.fold(
        problem => {
          pinnedSender ! CommitDomainEventsFailed(problem)
          log.error(problem.toString())
        },
        succ => {
          val lap = start.lap
          if (lap > writeWarnThreshold)
            log.warning(s"""Storing ${events.size} domain events took longer than ${writeWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
          statisticsCollector.foreach(_ ! AddWriteDuration(lap))
          pinnedSender ! CommittedDomainEvents(events)
        })

    case GetAllDomainEvents =>
      fetchAndDispatchDomainEvents(BSONDocument(), noSorting, sender)

    case GetDomainEvent(eventId) =>
      val pinnedSender = sender
      val collection = db(collectionName)
      val query = BSONDocument("_id" -> BSONBinary(UuidConverter.uuidToBytes(eventId), Subtype.UuidSubtype))
      val res =
        for {
          docs <- collection.find(query).cursor.toList.toSuccessfulAlmFuture
          domainEvent <- AlmFuture {
            docs match {
              case Nil => None.success
              case d :: Nil => documentToDomainEvent(d).map(Some(_))
              case x => PersistenceProblem(s"""Expected 1 domain event with id "$eventId" but found ${x.size}.""").failure
            }
          }(serializationExecutor)
        } yield domainEvent
      res.onComplete(
        problem => {
          pinnedSender ! DomainEventQueryFailed(eventId, problem)
          log.error(problem.toString())
        },
        eventOpt => pinnedSender ! QueriedDomainEvent(eventId, eventOpt))

    case GetAllDomainEventsFor(aggId) =>
      val query = BSONDocument("aggid" -> uuidToBson(aggId))
      fetchAndDispatchDomainEvents(query, sortByVersion, sender)

    case GetDomainEventsFrom(aggId, fromVersion) =>
      val query = BSONDocument(
        "aggid" -> uuidToBson(aggId),
        "version" -> BSONDocument("$gte" -> BSONLong(fromVersion)))
      fetchAndDispatchDomainEvents(query, sortByVersion, sender)

    case GetDomainEventsTo(aggId, toVersion) =>
      val query = BSONDocument(
        "aggid" -> uuidToBson(aggId),
        "version" -> BSONDocument("$lte" -> BSONLong(toVersion)))
      fetchAndDispatchDomainEvents(query, sortByVersion, sender)

    case GetDomainEventsUntil(aggId, untilVersion) =>
      val query = BSONDocument(
        "aggid" -> uuidToBson(aggId),
        "version" -> BSONDocument("$lt" -> BSONLong(untilVersion)))
      fetchAndDispatchDomainEvents(query, sortByVersion, sender)

    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      val query = BSONDocument(
        "aggid" -> uuidToBson(aggId),
        "$and" -> BSONDocument(
          "version" -> BSONDocument(
            "$gte" -> BSONLong(fromVersion)),
          "version" -> BSONDocument(
            "$lte" -> BSONLong(toVersion))))
      fetchAndDispatchDomainEvents(query, sortByVersion, sender)

    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      val query = BSONDocument(
        "aggid" -> uuidToBson(aggId),
        "$and" -> BSONDocument(
          "version" -> BSONDocument(
            "$gte" -> BSONLong(fromVersion)),
          "version" -> BSONDocument(
            "$lt" -> BSONLong(untilVersion))))
      fetchAndDispatchDomainEvents(query, sortByVersion, sender)
  }

  override def receive = uninitialized

  override def preStart() {
    super.preStart()
    self ! Initialize
  }
}