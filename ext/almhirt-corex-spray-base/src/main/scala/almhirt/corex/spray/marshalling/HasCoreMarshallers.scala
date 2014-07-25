package almhirt.corex.spray.marshalling

import almhirt.core.types.serialization.HasCoreHttpSerializers
import almhirt.httpx.spray.marshalling.ContentTypeBoundMarshallerFactory
import almhirt.core.types._
import spray.httpx.marshalling.Marshaller

trait HasCoreMarshallers {
  def domainEventMarshaller: Marshaller[DomainEvent]
  implicit def executionStateMarshaller: Marshaller[ExecutionState]

  implicit def domainEventsMarshaller: Marshaller[Seq[DomainEvent]]
  implicit def domainCommandsMarshaller: Marshaller[Seq[DomainCommand]]
  implicit def executionStatesMarshaller: Marshaller[Seq[ExecutionState]]
}

trait CoreMarshallerInstances {self : HasCoreMarshallers =>
  def coreHttpSerializers: HasCoreHttpSerializers
  def coreContentTypeProviders: HasCoreContentTypeProviders
  override lazy val domainEventMarshaller: Marshaller[DomainEvent] = 
    ContentTypeBoundMarshallerFactory[DomainEvent](coreContentTypeProviders.domainEventContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventMarshallingInst).marshaller(coreHttpSerializers.domainEventHttpSerializer)

  override lazy val executionStateMarshaller: Marshaller[ExecutionState] = 
    ContentTypeBoundMarshallerFactory[ExecutionState](coreContentTypeProviders.executionStateContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStateMarshallingInst).marshaller(coreHttpSerializers.executionStateHttpSerializer)

  override lazy val domainEventsMarshaller: Marshaller[Seq[DomainEvent]] = 
    ContentTypeBoundMarshallerFactory[Seq[DomainEvent]](coreContentTypeProviders.domainEventsContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventsMarshallingInst).marshaller(coreHttpSerializers.domainEventsHttpSerializer)
    
  override lazy val domainCommandsMarshaller: Marshaller[Seq[DomainCommand]] = 
    ContentTypeBoundMarshallerFactory[Seq[DomainCommand]](coreContentTypeProviders.domainCommandsContentTypeProvider, DefaultCoreMarshallingInstances.DomainCommandsMarshallingInst).marshaller(coreHttpSerializers.domainCommandsHttpSerializer)
    
  override lazy val executionStatesMarshaller: Marshaller[Seq[ExecutionState]] = 
    ContentTypeBoundMarshallerFactory[Seq[ExecutionState]](coreContentTypeProviders.executionStatesContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStatesMarshallingInst).marshaller(coreHttpSerializers.executionStatesHttpSerializer)
}