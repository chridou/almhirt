package almhirt.eventlog

import java.util.UUID
import almhirt.common._
import almhirt.domain.DomainEvent

sealed trait DomainEventLogCmd
case class LogEventsQry(events: IndexedSeq[DomainEvent], correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetAllDomainEventsQry(chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetDomainEventsQry(aggId: UUID, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetDomainEventsFromQry(aggId: UUID, from: Long, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
case class GetDomainEventsFromToQry(aggId: UUID, from: Long, to: Long, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd

sealed trait DomainEventLogRsp
case class DomainEventsForAggregateRootRsp(aggId: UUID, chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
case class AllDomainEventsRsp(chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
case class LoggedDomainEventsRsp(committedEvents: IndexedSeq[DomainEvent], uncommittedEvents: Option[(Problem, IndexedSeq[DomainEvent])], correlationId: Option[UUID]) extends DomainEventLogRsp
