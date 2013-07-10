package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent

sealed trait FetchedDomainEvents {
  def index: Int
  def isLast: Boolean
}

final case class DomainEventsChunk(
  /**
   * Starts with Zero
   */
  index: Int,
  isLast: Boolean,
  events: Iterable[DomainEvent]) extends FetchedDomainEvents
  
final case class DomainEventsFailure(
  /**
   * Starts with Zero
   */
  index: Int,
  isLast: Boolean,
  problem: Problem) extends FetchedDomainEvents
  
object DomainEventLog {
  final case class LogDomainEvents(events: IndexedSeq[DomainEvent])
  final case class GetDomainEvent(eventId: JUUID)
  final case class GetAllDomainEvents()
  final case class GetDomainEvents(aggId: JUUID)
  final case class GetDomainEventsFrom(aggId: JUUID, from: Long)
  final case class GetDomainEventsTo(aggId: JUUID, to: Long)
  final case class GetDomainEventsFromTo(aggId: JUUID, from: Long, to: Long) 

//  sealed trait DomainEventLogRsp extends DomainEventLogMessage
//  final case class DomainEventsForAggregateRootRsp(aggId: UUID, chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
//  final case class AllDomainEventsRsp(chunk: DomainEventsChunk, correlationId: Option[UUID]) extends DomainEventLogRsp
//  final case class DomainEventByIdRsp(event: AlmValidation[DomainEvent], correlationId: Option[UUID]) extends DomainEventLogRsp
//  final case class LoggedDomainEventsRsp(committedEvents: IndexedSeq[DomainEvent], uncommittedEvents: Option[(Problem, IndexedSeq[DomainEvent])], correlationId: Option[UUID]) extends DomainEventLogRsp

}