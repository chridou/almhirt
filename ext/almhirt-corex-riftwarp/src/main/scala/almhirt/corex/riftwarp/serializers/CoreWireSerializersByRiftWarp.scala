package almhirt.corex.riftwarp.serializers

import almhirt.core.types._
import almhirt.core.types.serialization.HasCoreHttpSerializers
import riftwarp.HasRiftWarp
import riftwarp.util.WarpHttpSerializer

trait CoreHttpSerializersByRiftWarp extends HasCoreHttpSerializers { self: HasRiftWarp =>
  implicit override val domainEventHttpSerializer = WarpHttpSerializer[DomainEvent](myRiftWarp)
  implicit override val executionStateHttpSerializer =  WarpHttpSerializer[ExecutionState](myRiftWarp)
  implicit override val domainEventsHttpSerializer =  WarpHttpSerializer.collection[DomainEvent](myRiftWarp)
  implicit override val domainCommandsHttpSerializer =  WarpHttpSerializer.collection[DomainCommand](myRiftWarp)
  implicit override val executionStatesHttpSerializer =  WarpHttpSerializer.collection[ExecutionState](myRiftWarp)
}