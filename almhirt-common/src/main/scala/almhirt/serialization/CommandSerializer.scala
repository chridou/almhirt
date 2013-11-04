package almhirt.serialization

import almhirt.common.Command

trait CommandStringSerializer extends StringBasedSerializer[Command, Command]
trait CommandBinarySerializer extends BinaryBasedSerializer[Command, Command]
