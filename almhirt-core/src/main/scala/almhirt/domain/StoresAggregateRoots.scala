package almhirt.domain

import almhirt._

trait StoresAggregateRoots[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  def storeAndRetrieveUpdated(ar: AR, uncommittedEvents: List[Event]): AlmFuture[AR]
  def store(ar: AR, uncommittedEvents: List[Event], ticket: Option[String]): Unit
  def storeTracked(ar: AR, uncommittedEvents: List[Event], ticket: String) { store(ar, uncommittedEvents, Some(ticket)) }
  def storeUntracked(ar: AR, uncommittedEvents: List[Event]) { store(ar, uncommittedEvents, None) }
}