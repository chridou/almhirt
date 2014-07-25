package almhirt.core.types.serialization

import almhirt.http._
import almhirt.core.types._

trait HasCoreHttpSerializers {
  def domainEventHttpSerializer: HttpSerializer[DomainEvent] with HttpDeserializer[DomainEvent]
  implicit def executionStateHttpSerializer: HttpSerializer[ExecutionState] with HttpDeserializer[ExecutionState]
  implicit def domainEventsHttpSerializer: HttpSerializer[Seq[DomainEvent]] with HttpDeserializer[Seq[DomainEvent]]
  implicit def domainCommandsHttpSerializer: HttpSerializer[Seq[DomainCommand]] with HttpDeserializer[Seq[DomainCommand]]
  implicit def executionStatesHttpSerializer: HttpSerializer[Seq[ExecutionState]] with HttpDeserializer[Seq[ExecutionState]]
}