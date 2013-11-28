package almhirt.core.types.serialization

import almhirt.serialization.WireSerializer
import almhirt.core.types._

trait HasCoreWireSerializers {
  implicit def domainEventWireSerializer: WireSerializer[DomainEvent, DomainEvent]
  implicit def executionStateWireSerializer: WireSerializer[ExecutionState, ExecutionState]
  implicit def domainEventsWireSerializer: WireSerializer[Seq[DomainEvent], Seq[DomainEvent]]
  implicit def executionStatesWireSerializer: WireSerializer[Seq[ExecutionState], Seq[ExecutionState]]
}