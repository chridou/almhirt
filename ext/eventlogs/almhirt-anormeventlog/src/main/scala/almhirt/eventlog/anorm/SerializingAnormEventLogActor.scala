package almhirt.eventlog.anorm

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.messaging.Message
import almhirt._
import almhirt.environment.AlmhirtContext
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._

class SerializingAnormEventLogActor(implicit almhirtContext: AlmhirtContext) extends Actor {
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
