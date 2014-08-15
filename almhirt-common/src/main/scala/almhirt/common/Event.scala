package almhirt.common

import org.joda.time.LocalDateTime
import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }
import almhirt.aggregates.AggregateRootId
import almhirt.aggregates.AggregateRootVersion

case class EventHeader(id: EventId, timestamp: LocalDateTime, metadata: Map[String, String])

object EventHeader {
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader =
    EventHeader(EventId(ccuad.getUniqueString), ccuad.getUtcTimestamp, Map.empty)
    
  def apply(id: EventId)(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader =
    EventHeader(id, ccuad.getUtcTimestamp, Map.empty)
    
  def apply(id: EventId, timestamp: LocalDateTime): EventHeader =
    EventHeader(id, timestamp, Map.empty)
    
  implicit class EventHeaderOps(self: EventHeader) {
    def withMetadata(metadata: Map[String, String]): EventHeader =
      self.copy(metadata = metadata)
  }
}

trait Event {
  def header: EventHeader
  final def eventId: EventId = header.id
  final def timestamp: LocalDateTime = header.timestamp
}

trait DomainEvent extends Event

trait AggregateEvent extends DomainEvent {
  def aggId: AggregateRootId
  def aggVersion: AggregateRootVersion
}

object AggregateEvent {
  def unapply(d: Event): Option[(EventHeader, AggregateRootId, AggregateRootVersion)] =
    d match {
      case d: AggregateEvent =>
        Some(d.header, d.aggId, d.aggVersion)
      case _ => None
    }
}

trait SystemEvent extends Event
