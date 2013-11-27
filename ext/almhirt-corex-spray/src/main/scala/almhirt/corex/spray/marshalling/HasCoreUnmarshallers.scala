package almhirt.corex.spray.marshalling

import almhirt.core.serialization.HasCoreWireSerializers
import almhirt.httpx.spray.marshalling.ContentTypeBoundUnmarshallerFactory
import almhirt.domain.DomainEvent
import almhirt.commanding.ExecutionState

trait HasCoreUnmarshallers  { self: HasCoreWireSerializers with HasCoreContentTypeProviders =>
  import DefaultCoreMarshallingInstances._
  implicit val domainEventUnmarshaller = ContentTypeBoundUnmarshallerFactory[DomainEvent].unmarshaller
  implicit val executionStateUnmarshaller = ContentTypeBoundUnmarshallerFactory[ExecutionState].unmarshaller

  implicit val domainEvenstUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[DomainEvent]].unmarshaller
  implicit val executionStatesUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[ExecutionState]].unmarshaller
}