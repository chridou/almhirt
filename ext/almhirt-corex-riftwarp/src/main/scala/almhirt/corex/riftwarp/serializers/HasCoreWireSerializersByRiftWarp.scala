package almhirt.corex.riftwarp.serializers

import almhirt.core.serialization.HasCoreWireSerializers
import almhirt.domain.DomainEvent
import almhirt.commanding.ExecutionState
import riftwarp.HasRiftWarp
import riftwarp.util.WarpWireSerializer

trait HasCoreWireSerializersByRiftWarp extends HasCoreWireSerializers { self: HasRiftWarp =>
  implicit override val domainEventWireSerializer = WarpWireSerializer[DomainEvent, DomainEvent](myRiftWarp)
  implicit override val executionStateWireSerializer =  WarpWireSerializer[ExecutionState, ExecutionState](myRiftWarp)
  implicit override val domainEventsWireSerializer =  WarpWireSerializer.collection[DomainEvent](myRiftWarp)
  implicit override val executionStatesWireSerializer =  WarpWireSerializer.collection[ExecutionState](myRiftWarp)
}