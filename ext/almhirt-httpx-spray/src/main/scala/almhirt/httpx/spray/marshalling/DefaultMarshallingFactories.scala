package almhirt.httpx.spray.marshalling

import almhirt.common._

object EventMarshalling extends MarshallingFactory[Event]
object CommandMarshalling extends MarshallingFactory[Command]
object ProblemMarshalling extends MarshallingFactory[almhirt.common.Problem]
object UuidsMarshalling extends MarshallingFactory[Seq[java.util.UUID]]
