package almhirt.eventlog

import java.util.UUID
import almhirt.domain.DomainEvent
import almhirt.AlmValidation

sealed trait DomainEventLogCommands
case class LogEvents(events: List[DomainEvent]) extends DomainEventLogCommands
case object GetAllEvents extends DomainEventLogCommands
case class GetEvents(aggId: UUID) extends DomainEventLogCommands
case class GetEventsFrom(aggId: UUID, from: Long) extends DomainEventLogCommands
case class GetEventsFromTo(aggId: UUID, from: Long, to: Long) extends DomainEventLogCommands
case class GetRequiredNextEventVersion(aggId: UUID) extends DomainEventLogCommands

case class DomainEventsChunk(
  /** Starts with Zero 
   */
  index: Int,
  isLast: Boolean,
  events: AlmValidation[Iterable[DomainEvent]])

case class EventsForAggregateRoot(aggId: UUID, chunk: DomainEventsChunk)
case class AllEvents(chunk: DomainEventsChunk)
case class RequiredNextEventVersion(aggId: UUID, nextVersion: AlmValidation[Long])

trait DomainEventLog extends HasDomainEvents with CanStoreDomainEvents with almhirt.ActorBased