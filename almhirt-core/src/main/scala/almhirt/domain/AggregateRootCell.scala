package almhirt.domain

import akka.actor._

object AggregateRootCell {
  final case class UpdateAggregateRoot(ar: IsAggregateRoot, events: Iterable[DomainEvent])
  case object GetAggregateRoot
  final case class RequestedAggregateRoot(ar: IsAggregateRoot)
  case class CheckAggregateRootAge(maxAge: org.joda.time.Duration, againstTime: org.joda.time.DateTime)
}

trait AggregateRootCell { self: Actor =>
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]
  
  protected def receiveAggregateRootCellMsg: Receive
}