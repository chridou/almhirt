package almhirt.eventlog

import java.util.UUID
import almhirt.common._
import almhirt.core._
import almhirt.domain.DomainEvent

sealed trait DomainEventLogCmd
case class LogEventsQry(events: List[DomainEvent], executionIdent: Option[UUID] = None) extends DomainEventLogCmd
case class GetAllEventsQry(chunkSize: Option[Int] = None, executionIdent: Option[UUID] = None) extends DomainEventLogCmd
case class GetEventsQry(aggId: UUID, chunkSize: Option[Int] = None, executionIdent: Option[UUID] = None) extends DomainEventLogCmd
case class GetEventsFromQry(aggId: UUID, from: Long, chunkSize: Option[Int] = None, executionIdent: Option[UUID] = None) extends DomainEventLogCmd
case class GetEventsFromToQry(aggId: UUID, from: Long, to: Long, chunkSize: Option[Int] = None, executionIdent: Option[UUID] = None) extends DomainEventLogCmd
case class GetRequiredNextEventVersionQry(aggId: UUID) extends DomainEventLogCmd

sealed trait DomainEventLogRsp
case class EventsForAggregateRootRsp(aggId: UUID, chunk: DomainEventsChunk, executionIdent: Option[UUID]) extends DomainEventLogRsp
case class AllEventsRsp(chunk: DomainEventsChunk, executionIdent: Option[UUID]) extends DomainEventLogRsp
case class RequiredNextEventVersionRsp(aggId: UUID, nextVersion: AlmValidation[Long]) extends DomainEventLogRsp
case class CommittedDomainEventsRsp(events: AlmValidation[Iterable[DomainEvent]], executionIdent: Option[UUID]) extends DomainEventLogRsp
case class PurgedDomainEventsRsp(events: AlmValidation[Iterable[DomainEvent]], executionIdent: Option[UUID]) extends DomainEventLogRsp

case class DomainEventsChunk(
  /**
   * Starts with Zero
   */
  index: Int,
  isLast: Boolean,
  events: AlmValidation[Iterable[DomainEvent]])

trait DomainEventLog extends HasDomainEvents with CanStoreDomainEvents with almhirt.common.ActorBased with Closeable

object DomainEventLog {
  import scalaz.syntax.validation._
  import akka.actor._
  import almhirt.environment._
  import almhirt.eventlog.impl._

  def apply()(implicit almhirt: Almhirt, system: AlmhirtSystem): AlmValidation[DomainEventLog] = unsafeInMemory()

  def unsafeInMemory()(implicit baseOps: AlmhirtBaseOps, system: AlmhirtSystem): AlmValidation[DomainEventLog] = {
    new InefficientSerializingInMemoryDomainEventLogFactory().create(baseOps, system)
  }

  def devNull()(implicit system: AlmhirtSystem): AlmValidation[DomainEventLog] = {
    new DevNullEventLogFactory().create(system)
  }

}