package almhirt.corex.spray.marshalling

import almhirt.core.types.serialization.HasCoreHttpSerializers
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
  def coreHttpSerializers: HasCoreHttpSerializers
  def coreContentTypeProviders: HasCoreContentTypeProviders
  override lazy val domainEventUnmarshaller: Unmarshaller[DomainEvent] =
    ContentTypeBoundUnmarshallerFactory[DomainEvent](coreContentTypeProviders.domainEventContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventMarshallingInst).unmarshaller(coreHttpSerializers.domainEventHttpSerializer)

  override lazy val executionStateUnmarshaller: Unmarshaller[ExecutionState] =
    ContentTypeBoundUnmarshallerFactory[ExecutionState](coreContentTypeProviders.executionStateContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStateMarshallingInst).unmarshaller(coreHttpSerializers.executionStateHttpSerializer)

  override lazy val domainEventsUnmarshaller: Unmarshaller[Seq[DomainEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[DomainEvent]](coreContentTypeProviders.domainEventsContentTypeProvider, DefaultCoreMarshallingInstances.DomainEventsMarshallingInst).unmarshaller(coreHttpSerializers.domainEventsHttpSerializer)

  override lazy val domainCommandsUnmarshaller: Unmarshaller[Seq[DomainCommand]] =
    ContentTypeBoundUnmarshallerFactory[Seq[DomainCommand]](coreContentTypeProviders.domainCommandsContentTypeProvider, DefaultCoreMarshallingInstances.DomainCommandsMarshallingInst).unmarshaller(coreHttpSerializers.domainCommandsHttpSerializer)

  override lazy val executionStatesUnmarshaller: Unmarshaller[Seq[ExecutionState]] =
    ContentTypeBoundUnmarshallerFactory[Seq[ExecutionState]](coreContentTypeProviders.executionStatesContentTypeProvider, DefaultCoreMarshallingInstances.ExecutionStatesMarshallingInst).unmarshaller(coreHttpSerializers.executionStatesHttpSerializer)
}