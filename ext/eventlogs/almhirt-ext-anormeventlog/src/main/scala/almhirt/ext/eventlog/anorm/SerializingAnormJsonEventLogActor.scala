package almhirt.ext.eventlog.anorm

import java.util.UUID
import java.util.Properties
import java.sql.{ DriverManager, Connection, Timestamp }
import scalaz._, Scalaz._
import scalaz.syntax.validation
import scalaz.std._
import org.joda.time.DateTime
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import riftwarp.RiftJson
import riftwarp.DimensionString
import riftwarp.DimensionString
import riftwarp.RiftWarp
import almhirt.almakka.AlmActorLogging
import _root_.anorm._

class SerializingAnormJsonEventLogActor(settings: AnormSettings)(implicit riftWarp: RiftWarp, uuidsAndTime: CanCreateUuidsAndDateTimes) extends Actor with CanLogProblems with AlmActorLogging {
  private var loggedEvents: List[DomainEvent] = Nil

  private val cmdInsert = "INSERT INTO %s(id, aggId, aggVersion, channel, timestamp, payload) VALUES({id}, {aggId}, {aggVersion}, {channel}, {timestamp}, {payload})".format(settings.logTableName)
  private val cmdNextId = "SELECT MAX(aggVersion)+1 AS next FROM %s e WHERE e.aggId = {aggId}".format(settings.logTableName)
  private val qryAllEvents = "SELECT * FROM %s".format(settings.logTableName)
  private val qryAllEventsFor = "SELECT * FROM %s e WHERE e.aggId = {aggId}".format(settings.logTableName)
  private val qryAllEventsForFrom = "SELECT * FROM %s e WHERE e.aggId = {aggId} AND e.aggVersion >= {from}".format(settings.logTableName)
  private val qryAllEventsForFromTo = "SELECT * FROM %s e WHERE e.aggId = {aggId} AND e.aggVersion >= {from} AND e.aggVersion <= {to}".format(settings.logTableName)

  private def getConnection() = {
    try {
      DriverManager.getConnection(settings.connection, settings.props).success
    } catch {
      case exn: Exception => PersistenceProblem("Could not connect to %s".format(settings.connection), cause = Some(exn)).failure
    }
  }

  private def withConnection[T](compute: Connection => AlmValidation[T]): AlmValidation[T] =
    DbUtil.withConnection(getConnection)(compute)

  private def inTransaction[T](compute: Connection => AlmValidation[T]): AlmValidation[T] =
    DbUtil.inTransaction(withConnection[T])(compute)

  private def storeEvents(events: IndexedSeq[DomainEvent]): AlmValidation[IndexedSeq[DomainEvent]] = {
    val entriesV = AnormEventLogEntry.fromDomainEvents(events)
    entriesV.flatMap(entries =>
      inTransaction(implicit conn => {
        val rowsInserted =
          entries.map { entry =>
            val timestamp = new Timestamp(entry.timestamp.getMillis())
            val cmd = SQL(cmdInsert).on("id" -> entry.id, "aggId" -> entry.aggId, "aggVersion" -> entry.aggVersion, "timestamp" -> timestamp, "channel" -> RiftJson().channelType, "payload" -> entry.payload.toString)
            cmd.executeInsert()
          }.flatten
        // This one has to be observed. Does insert always return 0 within a transaction?
        //          Tests show, that there was a row inserted.  
        //        if (rowsInserted.length == events.length)
        //          events.success
        //        else
        //          PersistenceProblem("Number of committed events(%d) does not match the number of events to store(%d)!".format(rowsInserted.length, events.length)).failure
        events.toVector.success
      }))
  }

  private def getEventsFor(id: UUID, from: Option[Long], to: Option[Long]): AlmValidation[Iterable[DomainEvent]] = {
    val cmd =
      (from, to) match {
        case (None, None) => SQL(qryAllEventsFor).on("aggId" -> id)
        case (Some(from), None) => SQL(qryAllEventsForFrom).on("aggId" -> id, "from" -> from)
        case (None, Some(to)) => SQL(qryAllEventsForFromTo).on("aggId" -> id, "from" -> 0, "to" -> to)
        case (Some(from), Some(to)) => SQL(qryAllEventsForFromTo).on("aggId" -> id, "from" -> from, "to" -> to)
      }
    val result = withConnection { implicit conn =>
      val payloadsV =
        cmd().map { row =>
          val str = inTryCatch { row[String]("payload") }
          str.flatMap(x =>
            riftWarp.receiveFromWarp[DimensionString, DomainEvent](RiftJson())(DimensionString(x)))
        }.map(_.toAgg).toList
      payloadsV.sequence
    }
    result
  }

  private def getAllEvents(): AlmValidation[Iterable[DomainEvent]] = {
    val cmd = SQL(qryAllEvents)
    val result = withConnection { implicit conn =>
      val payloadsV =
        cmd().map { row =>
          val str = inTryCatch { row[String]("payload") }
          str.flatMap(x =>
            riftWarp.receiveFromWarp[DimensionString, DomainEvent](RiftJson())(DimensionString(x)))
        }.map(_.toAgg).toList
      payloadsV.sequence
    }
    result
  }

  def receive: Receive = {
    case LogEventsQry(events, executionIdent) =>
      val res = storeEvents(events).onResult(
        fail => sender ! LoggedDomainEventsRsp(Vector.empty, Some((fail, events)), executionIdent),
        succ => sender ! LoggedDomainEventsRsp(succ, None, executionIdent))

    case GetAllDomainEventsQry(chunkSize, execIdent) =>
      val res = getAllEvents()
      sender ! AllDomainEventsRsp(DomainEventsChunk(0, true, res), execIdent)
    case GetDomainEventsQry(aggId, chunkSize, execIdent) =>
      val res = getEventsFor(aggId, None, None)
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
    case GetDomainEventsFromQry(aggId, from, chunkSize, execIdent) =>
      val res = getEventsFor(aggId, Some(from), None)
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
    case GetDomainEventsFromToQry(aggId, from, to, chunkSize, execIdent) =>
      val res = getEventsFor(aggId, Some(from), Some(to))
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
  }

}
