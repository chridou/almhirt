package almhirt.core.serialization

import almhirt.serialization.WireSerializer
import almhirt.domain.DomainEvent
import almhirt.commanding.ExecutionState

trait HasCoreWireSerializers {
  implicit def domainEventWireSerializer: WireSerializer[DomainEvent, DomainEvent]
  implicit def executionStateWireSerializer: WireSerializer[ExecutionState, ExecutionState]
  implicit def domainEventsWireSerializer: WireSerializer[Seq[DomainEvent], Seq[DomainEvent]]
  implicit def executionStatesWireSerializer: WireSerializer[Seq[ExecutionState], Seq[ExecutionState]]
}