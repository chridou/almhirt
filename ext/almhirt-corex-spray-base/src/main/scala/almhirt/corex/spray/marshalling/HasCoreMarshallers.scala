package almhirt.corex.spray.marshalling

import almhirt.core.types.serialization.HasCoreWireSerializers
import almhirt.httpx.spray.marshalling.ContentTypeBoundMarshallerFactory
import almhirt.core.types._
import spray.httpx.marshalling.Marshaller

trait HasCoreMarshallers {
  def domainEventMarshaller: Marshaller[DomainEvent]
  implicit def executionStateMarshaller: Marshaller[ExecutionState]

  implicit def domainEventsMarshaller: Marshaller[Seq[DomainEvent]]
  implicit def executionStatesMarshaller: Marshaller[Seq[ExecutionState]]
}

trait CoreMarshallerInstances {self : HasCoreMarshallers =>
  def coreWireSerializers: HasCoreWireSerializers
  def coreContentTypeProviders: HasCoreContentTypeProviders
  override lazy val domainEventMarshaller: Marshaller[DomainEvent] = 
    ContentTypeBoundMarshallerFactory[DomainEvent](coreContentTypeProviders.domainEventContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventMarshallingInst).marshaller(coreWireSerializers.domainEventWireSerializer)

  override lazy val executionStateMarshaller: Marshaller[ExecutionState] = 
    ContentTypeBoundMarshallerFactory[ExecutionState](coreContentTypeProviders.executionStateContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStateMarshallingInst).marshaller(coreWireSerializers.executionStateWireSerializer)

  override lazy val domainEventsMarshaller: Marshaller[Seq[DomainEvent]] = 
    ContentTypeBoundMarshallerFactory[Seq[DomainEvent]](coreContentTypeProviders.domainEventsContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventsMarshallingInst).marshaller(coreWireSerializers.domainEventsWireSerializer)
    
  override lazy val executionStatesMarshaller: Marshaller[Seq[ExecutionState]] = 
    ContentTypeBoundMarshallerFactory[Seq[ExecutionState]](coreContentTypeProviders.executionStatesContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStatesMarshallingInst).marshaller(coreWireSerializers.executionStatesWireSerializer)
}