package almhirt.corex.spray.marshalling

import almhirt.commanding.ExecutionState
import almhirt.domain.DomainEvent
import almhirt.httpx.spray.marshalling.MarshallingFactory

object DomainEventMarshalling extends MarshallingFactory[DomainEvent]
object ExecutionStateMarshalling extends MarshallingFactory[ExecutionState]
