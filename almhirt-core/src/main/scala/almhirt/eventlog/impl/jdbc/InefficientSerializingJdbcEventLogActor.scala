package almhirt.eventlog.impl.jdbc

import java.util.Properties
import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.environment.AlmhirtContext
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.riftwarp.RiftJson
import java.sql._

class InefficientSerializingJdbcEventLogActor(settings: JdbcEventLogSettings)(implicit almhirtContext: AlmhirtContext) extends Actor {
  Class.forName(settings.drivername)
  val logTableName = settings.logTableName.getOrElse("eventlog")

  private def createLogEntry(event: DomainEvent): AlmValidation[JdbcEventLogEntry] = {
    val serializedEvent = almhirtContext.riftWarp.prepareForWarp(RiftJson)(event)
    serializedEvent.map(serEvent =>
      JdbcEventLogEntry(event.id, event.version, almhirtContext.getDateTime, serEvent))
  }

  private def inConnection[T](compute: Connection => AlmValidation[T]): AlmValidation[T] = {
    computeSafely({
      val connection = DriverManager.getConnection(settings.connection, settings.props)
      compute(connection)
    }, PersistenceProblem(""))
  }

  private def inTransaction[T](compute: Connection => AlmValidation[T]): AlmValidation[T] = {
    inConnection { conn =>
      conn.setAutoCommit(false)
      try {
        val res = compute(conn)
        conn.commit()
        conn.setAutoCommit(true)
        res
      } catch {
        case exn =>
          conn.rollback
          conn.setAutoCommit(true)
          PersistenceProblem("Could not execute transaction", cause = Some(CauseIsThrowable(exn))).failure
      }
    }
  }

  private def storeEvents(events: List[DomainEvent]): AlmValidation[List[DomainEvent]] = {
    val entriesV = events.map(event => createLogEntry(event).toAgg).sequence
    entriesV.map(entries =>
      inTransaction(conn => {
        val statement = conn.createStatement()

        val count = statement.executeBatch()
        events.success
      }))
    sys.error("")
  }

  private var loggedEvents: List[DomainEvent] = Nil
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
