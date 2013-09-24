package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.common._

class DomainEventLogRouter(numChildren: Int, childProps: Props) extends Actor {
  import DomainEventLog._
  val children = (for (i <- 0 until (numChildren)) yield context.actorOf(childProps)).toVector

  private def dispatch(arId: JUUID, message: Any) {
    val target = Math.abs(arId.hashCode()) % numChildren
    children(target) forward message
  }

  override def receive: Receive = {
    case m: CommitDomainEvents =>
      if (m.events.isEmpty) {
        sender ! CommittedDomainEvents(Seq.empty)
      } else {
        val aggId = m.events.head.aggId
        if (m.events.exists(_.aggId != aggId))
          sender ! CommitDomainEventsFailed(UnspecifiedProblem("""All domain eveents to commit must have the same aggragate root id $aggId."""))
        else
          dispatch(aggId, m)
      }
    case m: GetAllDomainEvents.type => sender ! FetchedDomainEventsFailure(UnspecifiedProblem("""DomainEventLogRouter does not support "GetAllDomainEvents"."""))
    case m: GetDomainEvent => sender ! DomainEventQueryFailed(m.eventId, UnspecifiedProblem("""DomainEventLogRouter does not support "GetDomainEvent"."""))
    case m: GetAllDomainEventsFor => dispatch(m.aggId, m)
    case m: GetDomainEventsFrom => dispatch(m.aggId, m)
    case m: GetDomainEventsTo => dispatch(m.aggId, m)
    case m: GetDomainEventsUntil => dispatch(m.aggId, m)
    case m: GetDomainEventsFromTo => dispatch(m.aggId, m)
    case m: GetDomainEventsFromUntil => dispatch(m.aggId, m)
  }
}