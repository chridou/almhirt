package almhirt.domain

import almhirt._
import almhirt.util.TrackingTicket

trait StoresAggregateRoots[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  def store(ar: AR, uncommittedEvents: List[Event], ticket: Option[TrackingTicket]): Unit
  def storeTracked(ar: AR, uncommittedEvents: List[Event], ticket: TrackingTicket) { store(ar, uncommittedEvents, Some(ticket)) }
  def storeUntracked(ar: AR, uncommittedEvents: List[Event]) { store(ar, uncommittedEvents, None) }
}