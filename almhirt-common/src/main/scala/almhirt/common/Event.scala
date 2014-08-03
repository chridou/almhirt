package almhirt.common

import org.joda.time.LocalDateTime
import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }

trait Event {
  def id: EventId
  /** The events creation timestamp */
  def timestamp: LocalDateTime
}

trait DomainEvent extends Event
trait AggregateEvent extends DomainEvent {
  def aggId: AggregateRootId
  def aggVersion: AggregateRootVersion
}

trait SystemEvent extends Event
