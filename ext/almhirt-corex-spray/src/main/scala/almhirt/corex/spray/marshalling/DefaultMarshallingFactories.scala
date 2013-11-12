package almhirt.corex.spray.marshalling

import almhirt.common._
import almhirt.commanding.ExecutionState
import almhirt.domain.DomainEvent

object EventMarshalling extends MarshallingFactory[Event]
object DomainEventMarshalling extends MarshallingFactory[DomainEvent]
object CommandMarshalling extends MarshallingFactory[Command]
object ProblemMarshalling extends MarshallingFactory[almhirt.common.Problem]

object ExecutionStateMarshalling extends MarshallingFactory[ExecutionState]
