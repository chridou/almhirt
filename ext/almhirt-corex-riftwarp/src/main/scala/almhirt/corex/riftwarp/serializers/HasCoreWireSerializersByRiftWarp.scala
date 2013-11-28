package almhirt.corex.riftwarp.serializers

import almhirt.core.types._
import almhirt.core.types.serialization.HasCoreWireSerializers
import riftwarp.HasRiftWarp
import riftwarp.util.WarpWireSerializer

trait HasCoreWireSerializersByRiftWarp extends HasCoreWireSerializers { self: HasRiftWarp =>
  implicit override val domainEventWireSerializer = WarpWireSerializer[DomainEvent, DomainEvent](myRiftWarp)
  implicit override val executionStateWireSerializer =  WarpWireSerializer[ExecutionState, ExecutionState](myRiftWarp)
  implicit override val domainEventsWireSerializer =  WarpWireSerializer.collection[DomainEvent](myRiftWarp)
  implicit override val executionStatesWireSerializer =  WarpWireSerializer.collection[ExecutionState](myRiftWarp)
}