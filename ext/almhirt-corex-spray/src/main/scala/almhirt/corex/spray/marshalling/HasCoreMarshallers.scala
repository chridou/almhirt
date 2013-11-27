package almhirt.corex.spray.marshalling

import almhirt.core.serialization.HasCoreWireSerializers
import almhirt.httpx.spray.marshalling.ContentTypeBoundMarshallerFactory
import almhirt.domain.DomainEvent
import almhirt.commanding.ExecutionState

trait HasCoreMarshallers  { self: HasCoreWireSerializers with HasCoreContentTypeProviders =>
  import DefaultCoreMarshallingInstances._
  implicit val domainEventMarshaller = ContentTypeBoundMarshallerFactory[DomainEvent].marshaller
  implicit val executionStateMarshaller = ContentTypeBoundMarshallerFactory[ExecutionState].marshaller

  implicit val domainEvenstMarshaller = ContentTypeBoundMarshallerFactory[Seq[DomainEvent]].marshaller
  implicit val executionStatesMarshaller = ContentTypeBoundMarshallerFactory[Seq[ExecutionState]].marshaller
}