package almhirt.eventlog

import java.util.UUID
import almhirt.common._
import almhirt.domain.DomainEvent

sealed trait DomainEventLogCmd
case class LogEventsQry(events: IndexedSeq[DomainEvent], correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetAllEventsQry(chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetEventsQry(aggId: UUID, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetEventsFromQry(aggId: UUID, from: Long, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetEventsFromToQry(aggId: UUID, from: Long, to: Long, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd

sealed trait DomainEventLogRsp
case class EventsForAggregateRootRsp(aggId: UUID, chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
case class AllEventsRsp(chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
case class RequiredNextEventVersionRsp(aggId: UUID, nextVersion: AlmValidation[Long]) extends DomainEventLogRsp
case class LoggedDomainEventsRsp(committedEvents: IndexedSeq[DomainEvent], uncommittedEvents: Option[(Problem,IndexedSeq[DomainEvent])], correlationId: Option[UUID]) extends DomainEventLogRsp {
  def hasErrors = !uncommittedEvents.isEmpty
}
case class PurgedDomainEventsRsp(events: AlmValidation[IndexedSeq[DomainEvent]], correlationId: Option[UUID]) extends DomainEventLogRsp
