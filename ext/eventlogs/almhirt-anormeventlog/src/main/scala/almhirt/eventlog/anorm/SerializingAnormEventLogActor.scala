package almhirt.eventlog.anorm

import java.util.UUID
import java.util.Properties
import java.sql._
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment.AlmhirtContext
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import anorm._
import org.joda.time.DateTime
import almhirt.riftwarp.RiftJson

case class AnormSettings(connection: String, props: Properties)
case class AnormEventLogEntry(id: UUID, version: Long, timestamp: DateTime, payload: String)

class SerializingAnormEventLogActor(settings: AnormSettings)(implicit almhirtContext: AlmhirtContext) extends Actor {
  private var loggedEvents: List[DomainEvent] = Nil

  private def createLogEntry(event: DomainEvent): AlmValidation[AnormEventLogEntry] = {
    val serializedEvent = almhirtContext.riftWarp.prepareForWarp(RiftJson)(event)
    serializedEvent.map(serEvent =>
      AnormEventLogEntry(event.id, event.version, almhirtContext.getDateTime, serEvent))
  }

  private def inConnection[T](compute: Connection => AlmValidation[T]): AlmValidation[T] = {
    val connection =
      try {
        DriverManager.getConnection(settings.connection, settings.props).success
      } catch {
        case exn => PersistenceProblem("Could not connect to %s".format(settings.connection), cause = Some(CauseIsThrowable(exn))).failure
      }
    connection.bind(conn => {
      try {
        val res = compute(conn)
        conn.close()
        res
      } catch {
        case exn =>
          conn.close()
          PersistenceProblem("Could not execute a db operation", cause = Some(CauseIsThrowable(exn))).failure
      }
    })
  }

  //private def inTransaction[T](compute: Connection => AlmValidation[T]): AlmValidation[T] = {
  //    inConnection { conn =>
  //      conn.setAutoCommit(false)
  //      try {
  //        val res = compute(conn)
  //        conn.commit()
  //        conn.setAutoCommit(true)
  //        res
  //      } catch {
  //        case exn =>
  //          conn.rollback
  //          conn.setAutoCommit(true)
  //          PersistenceProblem("Could not execute transaction", cause = Some(CauseIsThrowable(exn))).failure
  //      }
  //    }
  //  }  

  //private def storeEvents(events: List[DomainEvent]): AlmValidation[List[DomainEvent]] = {
  //    val entriesV = events.map(event => createLogEntry(event).toAgg).sequence
  //    entriesV.bind(entries =>
  //      inTransaction(conn => {
  //        val statement = conn.prepareStatement("INSERT INTO %s VALUES(?, ?, ?, ?)".format(logTableName))
  //        entries.foreach(entry => {
  //          statement.setObject(1, entry.id)
  //          statement.setLong(2, entry.version)
  //          statement.setDate(3, new java.sql.Date(entry.timestamp.getMillis()))
  //          statement.setString(4, entry.payload)
  //          statement.execute()
  //          statement.clearParameters()
  //        })	
  //        events.success
  //      }))
  //  }

  def receive: Receive = {
    case LogEventsQry(events, executionIdent) =>
      loggedEvents = loggedEvents ++ events
      events.foreach(event => almhirtContext.broadcastDomainEvent(event))
      sender ! CommittedDomainEventsRsp(events.success, executionIdent)
    case GetAllEventsQry =>
      sender ! AllEventsRsp(DomainEventsChunk(0, true, loggedEvents.toIterable.success))
    case GetEventsQry(aggId) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(_.id == aggId).toIterable.success))
    case GetEventsFromQry(aggId, from) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.id == aggId && x.version >= from).toIterable.success))
    case GetEventsFromToQry(aggId, from, to) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.id == aggId && x.version >= from && x.version <= to).toIterable.success))
    case GetRequiredNextEventVersionQry(aggId) =>
      sender ! RequiredNextEventVersionRsp(aggId, loggedEvents.view.filter(x => x.id == aggId).lastOption.map(_.version + 1L).getOrElse(0L).success)
  }
}
