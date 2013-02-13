package almhirt.domain

import almhirt._
import almhirt.util.TrackingTicket

trait StoresAggregateRoots[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  def store(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]): Unit
  def storeTracked(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: TrackingTicket) { store(ar, uncommittedEvents, Some(ticket)) }
  def storeUntracked(ar: AR, uncommittedEvents: IndexedSeq[Event]) { store(ar, uncommittedEvents, None) }
}