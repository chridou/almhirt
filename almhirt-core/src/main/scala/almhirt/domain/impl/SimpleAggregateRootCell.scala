package almhirt.domain.impl

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import akka.actor._
import almhirt.domain.AggregateRootCell
import almhirt.domain._
import almhirt.domain.DomainEvent

trait SimpleAggregateRootCell extends AggregateRootCell { self: Actor =>
  import AggregateRootCell._

  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  def managedAggregateRooId: JUUID

  protected def domainEventLog: ActorRef

  protected def activeState(ar: AR, activeSince: DateTime): Receive = {
    case GetAggregateRoot =>
      sender ! RequestedAggregateRoot(ar)
    case UpdateAggregateRoot(ar, events) =>
      ???
    case CheckAggregateRootAge(maxAge, againstTime) =>
      if (activeSince.plus(maxAge).compareTo(againstTime) > 0)
        context.become(passiveState())
  }

  protected def waitingForEventLogState(): Receive = {
    case CheckAggregateRootAge(_, _) =>
      ()
  }

  protected def passiveState(): Receive = {
    case CheckAggregateRootAge(_, _) =>
      ()
  }

  protected def receiveAggregateRootCellMsg = passiveState()
}
  
  