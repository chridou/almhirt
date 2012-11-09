package almhirt.eventlog

import java.util.UUID
import almhirt.domain.DomainEvent
import almhirt.AlmValidation

sealed trait DomainEventLogCmd
case class LogEventsCmd(events: List[DomainEvent], executionIdent: Option[UUID]) extends DomainEventLogCmd
case object GetAllEventsCmd extends DomainEventLogCmd
case class GetEventsCmd(aggId: UUID) extends DomainEventLogCmd
case class GetEventsFromCmd(aggId: UUID, from: Long) extends DomainEventLogCmd
case class GetEventsFromToCmd(aggId: UUID, from: Long, to: Long) extends DomainEventLogCmd
case class GetRequiredNextEventVersionCmd(aggId: UUID) extends DomainEventLogCmd

sealed trait DomainEventLogRsp
case class EventsForAggregateRootRsp(aggId: UUID, chunk: DomainEventsChunk) extends DomainEventLogRsp
case class AllEventsRsp(chunk: DomainEventsChunk) extends DomainEventLogRsp
case class RequiredNextEventVersionRsp(aggId: UUID, nextVersion: AlmValidation[Long]) extends DomainEventLogRsp
case class CommittedDomainEventsRsp(events: Iterable[DomainEvent], executionIdent: Option[UUID]) extends DomainEventLogRsp
case class PurgedDomainEventsRsp(events: Iterable[DomainEvent], executionIdent: Option[UUID]) extends DomainEventLogRsp

case class DomainEventsChunk(
  /** Starts with Zero 
   */
  index: Int,
  isLast: Boolean,
  events: AlmValidation[Iterable[DomainEvent]])

trait DomainEventLog extends HasDomainEvents with CanStoreDomainEvents with almhirt.ActorBased