package almhirt.eventlog

import java.util.UUID
import almhirt.common._
import almhirt.domain.DomainEvent

sealed trait DomainEventLogMessage

sealed trait DomainEventLogCmd extends DomainEventLogMessage
final case class LogDomainEventsQry(events: IndexedSeq[DomainEvent], correlationId: Option[UUID] = None) extends DomainEventLogCmd
final case class GetAllDomainEventsQry(chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
final case class GetDomainEventsQry(aggId: UUID, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
final case class GetDomainEventsFromQry(aggId: UUID, from: Long, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd
final case class GetDomainEventsFromToQry(aggId: UUID, from: Long, to: Long, chunkSize: Option[Int] = None, correlationId: Option[UUID] = None) extends DomainEventLogCmd

sealed trait DomainEventLogRsp extends DomainEventLogMessage
final case class DomainEventsForAggregateRootRsp(aggId: UUID, chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
final case class AllDomainEventsRsp(chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
final case class LoggedDomainEventsRsp(committedEvents: IndexedSeq[DomainEvent], uncommittedEvents: Option[(Problem, IndexedSeq[DomainEvent])], correlationId: Option[UUID]) extends DomainEventLogRsp
