package almhirt.domain

import almhirt._

trait StoresAggregateRoots[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  def storeAndRetrieveUpdated(ar: AR, uncommitedEvents: List[Event]): AlmFuture[AR]
  def store(ar: AR, uncommitedEvents: List[Event], ticket: Option[String]): Unit
  def storeTracked(ar: AR, uncommitedEvents: List[Event], ticket: String) { store(ar, uncommitedEvents, Some(ticket)) }
  def storeUntracked(ar: AR, uncommitedEvents: List[Event]) { store(ar, uncommitedEvents, None) }
}