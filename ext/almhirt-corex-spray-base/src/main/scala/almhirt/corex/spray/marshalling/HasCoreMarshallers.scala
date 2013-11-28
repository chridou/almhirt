package almhirt.corex.spray.marshalling

import almhirt.core.types.serialization.HasCoreWireSerializers
import almhirt.httpx.spray.marshalling.ContentTypeBoundMarshallerFactory
import almhirt.core.types._

trait HasCoreMarshallers  { self: HasCoreWireSerializers with HasCoreContentTypeProviders =>
  import DefaultCoreMarshallingInstances._
  implicit val domainEventMarshaller = ContentTypeBoundMarshallerFactory[DomainEvent].marshaller
  implicit val executionStateMarshaller = ContentTypeBoundMarshallerFactory[ExecutionState].marshaller

  implicit val domainEvenstMarshaller = ContentTypeBoundMarshallerFactory[Seq[DomainEvent]].marshaller
  implicit val executionStatesMarshaller = ContentTypeBoundMarshallerFactory[Seq[ExecutionState]].marshaller
}