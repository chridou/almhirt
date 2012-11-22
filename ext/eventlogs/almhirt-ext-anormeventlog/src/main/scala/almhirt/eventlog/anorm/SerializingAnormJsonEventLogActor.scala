package almhirt.eventlog.anorm

import java.util.UUID
import java.util.Properties
import java.sql.{ DriverManager, Connection, Timestamp }
import scalaz._, Scalaz._
import scalaz.syntax.validation._
import scalaz.std._
import org.joda.time.DateTime
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment.AlmhirtContext
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.riftwarp.RiftJson
import _root_.anorm._
import almhirt.riftwarp.DimensionString
import almhirt.riftwarp.DimensionString

class SerializingAnormJsonEventLogActor(settings: AnormSettings)(implicit almhirtContext: AlmhirtContext) extends Actor {
  private var loggedEvents: List[DomainEvent] = Nil

  private val cmdInsert = "INSERT INTO %s(id, aggId, aggVersion, timestamp, payload) VALUES({id}, {aggId}, {aggVersion}, {timestamp}, {payload})".format(settings.logTableName)
  private val cmdNextId = "SELECT MAX(aggVersion)+1 AS next FROM %s e WHERE e.aggId = {aggId}".format(settings.logTableName)
  private val qryAllEvents = "SELECT * FROM %s".format(settings.logTableName)
  private val qryAllEventsFor = "SELECT * FROM %s e WHERE e.aggId = {aggId}".format(settings.logTableName)
  private val qryAllEventsForFrom = "SELECT * FROM %s e WHERE e.aggId = {aggId} AND e.aggVersion >= {from}".format(settings.logTableName)
  private val qryAllEventsForFromTo = "SELECT * FROM %s e WHERE e.aggId = {aggId} AND e.aggVersion >= {from} AND e.aggVersion <= {to}".format(settings.logTableName)

  private def getConnection() = {
    try {
      DriverManager.getConnection(settings.connection, settings.props).success
    } catch {
      case exn => PersistenceProblem("Could not connect to %s".format(settings.connection), cause = Some(CauseIsThrowable(exn))).failure
    }
  }

  private def withConnection[T](compute: Connection => AlmValidation[T]): AlmValidation[T] =
    DbUtil.withConnection(getConnection)(compute)

  private def inTransaction[T](compute: Connection => AlmValidation[T]): AlmValidation[T] =
    DbUtil.inTransaction(withConnection[T])(compute)

  private def storeEvents(events: List[DomainEvent]): AlmValidation[List[DomainEvent]] = {
    val entriesV = AnormEventLogEntry.fromDomainEvents(events)
    entriesV.bind(entries =>
      inTransaction(implicit conn => {
        val rowsInserted =
          entries.map { entry =>
            val timestamp = new Timestamp(entry.timestamp.getMillis())
            val cmd = SQL(cmdInsert).on("id" -> entry.id, "aggId" -> entry.aggId, "aggVersion" -> entry.aggVersion, "timestamp" -> timestamp, "payload" -> entry.payload.toString)
            cmd.executeInsert()
          }.flatten
// This one has to be observed. Does insert always return 0 within a transaction?
//          Tests show, that there was a row inserted.  
//        if (rowsInserted.length == events.length)
//          events.success
//        else
//          PersistenceProblem("Number of committed events(%d) does not match the number of events to store(%d)!".format(rowsInserted.length, events.length)).failure
          events.success
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
          str.bind(x =>
            almhirtContext.riftWarp.receiveFromWarp[DimensionString, DomainEvent](RiftJson())(x))
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
          str.bind(x =>
            almhirtContext.riftWarp.receiveFromWarp[DimensionString, DomainEvent](RiftJson())(x))
        }.map(_.toAgg).toList
      payloadsV.sequence
    }
    result
  }

  private def getNextRequiredVersion(aggId: UUID): AlmValidation[Long] = {
    withConnection { implicit conn =>
      val rowOpt = SQL(cmdNextId).on("aggId" -> aggId).apply().headOption
      option.cata(rowOpt)(v => inTryCatch { v[Option[Long]]("next").getOrElse(0L) }, 0L.success)
    }
  }

  def receive: Receive = {
    case LogEventsQry(events, executionIdent) =>
      val res = storeEvents(events)
      sender ! CommittedDomainEventsRsp(res, executionIdent)
    case GetAllEventsQry(chunkSize, execIdent) =>
      val res = getAllEvents()
      sender ! AllEventsRsp(DomainEventsChunk(0, true, res), execIdent)
    case GetEventsQry(aggId, chunkSize, execIdent) =>
      val res = getEventsFor(aggId, None, None)
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
    case GetEventsFromQry(aggId, from, chunkSize, execIdent) =>
      val res = getEventsFor(aggId, Some(from), None)
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
    case GetEventsFromToQry(aggId, from, to, chunkSize, execIdent) =>
      val res = getEventsFor(aggId, Some(from), Some(to))
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, res), execIdent)
    case GetRequiredNextEventVersionQry(aggId) =>
      val res = getNextRequiredVersion(aggId)
      sender ! RequiredNextEventVersionRsp(aggId, res)
  }
}
