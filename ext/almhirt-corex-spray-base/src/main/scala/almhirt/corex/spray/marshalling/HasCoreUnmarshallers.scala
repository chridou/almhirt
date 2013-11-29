package almhirt.corex.spray.marshalling

import almhirt.core.types.serialization.HasCoreWireSerializers
import almhirt.httpx.spray.marshalling.ContentTypeBoundUnmarshallerFactory
import almhirt.core.types._

trait HasCoreUnmarshallers  { self: HasCoreWireSerializers with HasCoreContentTypeProviders =>
  import DefaultCoreMarshallingInstances._
  val domainEventUnmarshaller = ContentTypeBoundUnmarshallerFactory[DomainEvent](domainEventContentTypeProvider, DomainEventMarshallingInst).unmarshaller(domainEventWireSerializer)
  implicit val executionStateUnmarshaller = ContentTypeBoundUnmarshallerFactory[ExecutionState].unmarshaller

  implicit val domainEvenstUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[DomainEvent]].unmarshaller
  implicit val executionStatesUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[ExecutionState]].unmarshaller
}