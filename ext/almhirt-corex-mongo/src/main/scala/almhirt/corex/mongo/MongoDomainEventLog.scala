package almhirt.corex.mongo

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
import reactivemongo.bson._
import reactivemongo.api._
import almhirt.common.Problem
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
    theAlmhirt: Almhirt): Props =
    Props(new MongoDomainEventLog(
      db,
      collectionName,
      serializeDomainEvent,
      deserializeDomainEvent,
      writeWarnThreshold,
      readWarnThreshold,
      serializationExecutor)(theAlmhirt))

  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument],
    deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent],
    writeWarnThreshold: FiniteDuration,
    readWarnThreshold: FiniteDuration,
    useNumberCruncherForSerialization: Boolean,
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
      theAlmhirt)
  }

  def props(db: DB with DBMetaCommands, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      collectionName <- configSection.v[String]("table-name")
      writeWarnThreshold <- configSection.v[FiniteDuration]("write-warn-threshold-duration")
      readWarnThreshold <- configSection.v[FiniteDuration]("read-warn-threshold-duration")
      useNumberCruncherForSerialization <- configSection.v[Boolean]("use-number-cruncher-for-serialization")
    } yield {
      theAlmhirt.log.info(s"""MongoDomainEventLog: table-name = $collectionName""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: write-warn-threshold-duration = ${writeWarnThreshold.defaultUnitString}""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: read-warn-threshold-duration = ${readWarnThreshold.defaultUnitString}""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: use-number-cruncher-for-serialization = $useNumberCruncherForSerialization""")
      propsRaw(db, collectionName, serializeDomainEvent, deserializeDomainEvent, writeWarnThreshold, readWarnThreshold, useNumberCruncherForSerialization, theAlmhirt)
    }

  def props(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      connections <- configSection.v[List[String]]("connections")
      dbName <- configSection.v[String]("db-name")
      db <- inTryCatch {
        val connection = driver.connection(connections)
        connection(dbName)(theAlmhirt.futuresExecutor)
      }
      props <- props(db, serializeDomainEvent, deserializeDomainEvent, configSection, theAlmhirt)
    } yield {
      theAlmhirt.log.info(s"""MongoDomainEventLog: connections = ${connections.mkString("[", ",", "]")}""")
      theAlmhirt.log.info(s"""MongoDomainEventLog: db-name = $dbName""")
      props
    }

  def props(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap { configSection =>
      props(driver, serializeDomainEvent, deserializeDomainEvent, configSection, theAlmhirt)
    }

  def props(driver: MongoDriver, serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], theAlmhirt: Almhirt): AlmValidation[Props] =
    props(driver, serializeDomainEvent, deserializeDomainEvent, "almhirt.domain-event-log", theAlmhirt)

  def props(serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      props(driver, serializeDomainEvent, deserializeDomainEvent, configPath, theAlmhirt))

  def props(serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument], deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent], theAlmhirt: Almhirt): AlmValidation[Props] =
    inTryCatch { new MongoDriver(theAlmhirt.actorSystem) }.flatMap(driver =>
      props(driver, serializeDomainEvent, deserializeDomainEvent, theAlmhirt))

}

class MongoDomainEventLog(
  db: DB with DBMetaCommands,
  collectionName: String,
  serializeDomainEvent: DomainEvent => AlmValidation[BSONDocument],
  deserializeDomainEvent: BSONDocument => AlmValidation[DomainEvent],
  writeWarnThreshold: FiniteDuration,
  readWarnThreshold: FiniteDuration,
  serializationExecutor: ExecutionContext)(implicit theAlmhirt: Almhirt) extends Actor with ActorLogging with DomainEventLog {
  import DomainEventLog._

  protected var writeStatistics = DomainEventLogWriteStatistics()
  protected var readStatistics = DomainEventLogReadStatistics()
  protected var serializationStatistics = DomainEventLogSerializationStatistics.forSerializing
  protected var deserializationStatistics = DomainEventLogSerializationStatistics.forDeserializing

  implicit val defaultExecutor = theAlmhirt.futuresExecutor

  override def publishCommittedEvent(domainEvent: DomainEvent) = theAlmhirt.messageBus.publish(domainEvent)

  val projectionFilter = BSONDocument("domainevent" -> 1)

  val noSorting = BSONDocument()
  val sortByVersion = BSONDocument("version" -> 1)

  def domainEventToDocument(domainEvent: DomainEvent): AlmValidation[BSONDocument] = {
    for {
      serialized <- serializeDomainEvent(domainEvent)
    } yield {
      BSONDocument(
        ("_id" -> BSONBinary(UuidConverter.uuidToBytes(domainEvent.eventId), Subtype.UuidSubtype)),
        ("aggid" -> BSONBinary(UuidConverter.uuidToBytes(domainEvent.aggId), Subtype.UuidSubtype)),
        ("version" -> BSONLong(domainEvent.aggVersion)),
        ("domainevent" -> serialized))
    }
  }

  def documentToDomainEvent(document: BSONDocument): AlmValidation[DomainEvent] = {
    document.get("domainevent") match {
      case Some(d: BSONDocument) => deserializeDomainEvent(d)
      case None => NoSuchElementProblem("BSONDocument for payload not found").failure
      case Some(x) => UnspecifiedProblem(s"""Payload must be contained as a BSONDocument. It is a "${x.getClass().getName()}".""").failure
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

  def commitDomainEvent(domainEvent: DomainEvent): AlmFuture[DomainEvent] =
    storeDomainEvent(domainEvent) andThen (
      fail => log.error(fail.toString()),
      succ => publishCommittedEvent(succ))

  def getEvents(query: BSONDocument, sort: BSONDocument): AlmFuture[Seq[DomainEvent]] = {
    val collection = db(collectionName)
    for {
      docs <- collection.find(query, projectionFilter).sort(sort).cursor.toList.toSuccessfulAlmFuture
      domainEvents <- AlmFuture {
        docs.map(x => documentToDomainEvent(x).toAgg).sequence
      }(serializationExecutor)
    } yield domainEvents
  }

  def fetchAndDispatchDomainEvents(query: BSONDocument, sort: BSONDocument) {
    getEvents(query, sort).fold(
      problem => {
        sender ! FetchedDomainEventsFailure(problem)
        log.error(problem.toString())
      },
      domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))
  }

  override def receiveDomainEventLogMsg: Receive = {
    case CommitDomainEvents(events) =>
      val committedEvents = AlmFuture.sequence(events.map(commitDomainEvent))
      committedEvents.fold(
        problem => {
          sender ! CommitDomainEventsFailed(problem)
          log.error(problem.toString())
        },
        succ => {
          sender ! CommittedDomainEvents(events)
        })

    case GetAllDomainEvents =>
      fetchAndDispatchDomainEvents(BSONDocument(), noSorting)

    case GetDomainEvent(eventId) =>
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
          sender ! DomainEventQueryFailed(eventId, problem)
          log.error(problem.toString())
        },
        eventOpt => sender ! QueriedDomainEvent(eventId, eventOpt))

    case GetAllDomainEventsFor(aggId) =>
      val query = BSONDocument("aggid" -> BSONBinary(UuidConverter.uuidToBytes(aggId), Subtype.UuidSubtype))
      fetchAndDispatchDomainEvents(query, sortByVersion)

    case GetDomainEventsFrom(aggId, fromVersion) =>
      val query = BSONDocument(
        "aggid" -> BSONBinary(UuidConverter.uuidToBytes(aggId), Subtype.UuidSubtype),
        "version" -> BSONDocument("$gte" -> BSONLong(fromVersion)))
      fetchAndDispatchDomainEvents(query, sortByVersion)

    case GetDomainEventsTo(aggId, toVersion) =>
      val query = BSONDocument(
        "aggid" -> BSONBinary(UuidConverter.uuidToBytes(aggId), Subtype.UuidSubtype),
        "version" -> BSONDocument("$lte" -> BSONLong(toVersion)))
      fetchAndDispatchDomainEvents(query, sortByVersion)

    case GetDomainEventsUntil(aggId, untilVersion) =>
      val query = BSONDocument(
        "aggid" -> BSONBinary(UuidConverter.uuidToBytes(aggId), Subtype.UuidSubtype),
        "version" -> BSONDocument("$lt" -> BSONLong(untilVersion)))
      fetchAndDispatchDomainEvents(query, sortByVersion)

    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      val query = BSONDocument(
        "aggid" -> BSONBinary(UuidConverter.uuidToBytes(aggId), Subtype.UuidSubtype),
        "$and" -> BSONDocument(
          "version" -> BSONDocument(
            "$gte" -> BSONLong(fromVersion)),
          "version" -> BSONDocument(
            "$lte" -> BSONLong(toVersion))))
      fetchAndDispatchDomainEvents(query, sortByVersion)

    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      val query = BSONDocument(
        "aggid" -> BSONBinary(UuidConverter.uuidToBytes(aggId), Subtype.UuidSubtype),
        "$and" -> BSONDocument(
          "version" -> BSONDocument(
            "$gte" -> BSONLong(fromVersion)),
          "version" -> BSONDocument(
            "$lt" -> BSONLong(untilVersion))))
      fetchAndDispatchDomainEvents(query, sortByVersion)
  }

  override def receive = receiveDomainEventLogMsg
}