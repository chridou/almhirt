package almhirt.serialization

import almhirt.common.Command

trait CommandStringSerializer extends StringSerializing[Command, Command]
trait CommandBinarySerializer extends BinarySerializing[Command, Command]