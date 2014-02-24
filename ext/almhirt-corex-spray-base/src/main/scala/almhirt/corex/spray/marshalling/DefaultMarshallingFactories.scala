package almhirt.corex.spray.marshalling

import almhirt.core.types._
import almhirt.httpx.spray.marshalling.MarshallingFactory

object DomainEventMarshalling extends MarshallingFactory[DomainEvent]
object DomainCommandsMarshalling extends MarshallingFactory[Seq[DomainCommand]]
object ExecutionStateMarshalling extends MarshallingFactory[ExecutionState]

object DomainEventsMarshalling extends MarshallingFactory[Seq[DomainEvent]]
object ExecutionStatesMarshalling extends MarshallingFactory[Seq[ExecutionState]]
