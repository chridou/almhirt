package almhirt.domain

import akka.actor._

object AggregateRootCell {
  final case class UpdateAggregateRoot(ar: IsAggregateRoot, events: Iterable[DomainEvent])
  case object GetAggregateRoot
  case object CheckAggregateRootAge
  
  final case class RequestedAggregateRoot(ar: IsAggregateRoot)
  case object UpdateFailed
}

trait AggregateRootCell { self: Actor =>
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]
  
  protected def receiveAggregateRootCellMsg: Receive
}