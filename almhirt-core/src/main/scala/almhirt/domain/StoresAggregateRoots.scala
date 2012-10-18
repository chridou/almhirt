package almhirt.domain

import almhirt._

trait StoresAggregateRoots[AR <: AggregateRoot[_,_], Event <: DomainEvent] {
  def store(ar: AR, uncommitedEvents: List[Event]): AlmFuture[AR]
  def store(ar: AR, uncommitedEvents: List[Event], ticket: Option[java.util.UUID]): Unit
}