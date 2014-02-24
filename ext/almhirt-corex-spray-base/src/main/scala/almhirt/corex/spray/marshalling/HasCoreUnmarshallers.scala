package almhirt.corex.spray.marshalling

import almhirt.core.types.serialization.HasCoreWireSerializers
import almhirt.httpx.spray.marshalling.ContentTypeBoundUnmarshallerFactory
import almhirt.core.types._
import spray.httpx.unmarshalling.Unmarshaller

trait HasCoreUnmarshallers {
  def domainEventUnmarshaller: Unmarshaller[DomainEvent]
  implicit def executionStateUnmarshaller: Unmarshaller[ExecutionState]

  implicit def domainEventsUnmarshaller: Unmarshaller[Seq[DomainEvent]]
  implicit def domainCommandsUnmarshaller: Unmarshaller[Seq[DomainCommand]]
  implicit def executionStatesUnmarshaller: Unmarshaller[Seq[ExecutionState]]
}

trait CoreUnmarshallerInstances { self: HasCoreUnmarshallers =>
  def coreWireSerializers: HasCoreWireSerializers
  def coreContentTypeProviders: HasCoreContentTypeProviders
  override lazy val domainEventUnmarshaller: Unmarshaller[DomainEvent] =
    ContentTypeBoundUnmarshallerFactory[DomainEvent](coreContentTypeProviders.domainEventContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventMarshallingInst).unmarshaller(coreWireSerializers.domainEventWireSerializer)

  override lazy val executionStateUnmarshaller: Unmarshaller[ExecutionState] =
    ContentTypeBoundUnmarshallerFactory[ExecutionState](coreContentTypeProviders.executionStateContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStateMarshallingInst).unmarshaller(coreWireSerializers.executionStateWireSerializer)

  override lazy val domainEventsUnmarshaller: Unmarshaller[Seq[DomainEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[DomainEvent]](coreContentTypeProviders.domainEventsContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventsMarshallingInst).unmarshaller(coreWireSerializers.domainEventsWireSerializer)

  override lazy val domainCommandsUnmarshaller: Unmarshaller[Seq[DomainCommand]] =
    ContentTypeBoundUnmarshallerFactory[Seq[DomainCommand]](coreContentTypeProviders.domainCommandsContentTypeProvider, DefaultCoreMarshallingInstances.DomainCommandsMarshallingInst).unmarshaller(coreWireSerializers.domainCommandsWireSerializer)

  override lazy val executionStatesUnmarshaller: Unmarshaller[Seq[ExecutionState]] =
    ContentTypeBoundUnmarshallerFactory[Seq[ExecutionState]](coreContentTypeProviders.executionStatesContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStatesMarshallingInst).unmarshaller(coreWireSerializers.executionStatesWireSerializer)
}