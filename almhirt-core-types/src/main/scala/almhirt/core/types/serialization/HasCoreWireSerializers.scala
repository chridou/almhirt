package almhirt.core.types.serialization

import almhirt.serialization.WireSerializer
import almhirt.core.types._

trait HasCoreWireSerializers {
  def domainEventWireSerializer: WireSerializer[DomainEvent]
  implicit def executionStateWireSerializer: WireSerializer[ExecutionState]
  implicit def domainEventsWireSerializer: WireSerializer[Seq[DomainEvent]]
  implicit def executionStatesWireSerializer: WireSerializer[Seq[ExecutionState]]
}