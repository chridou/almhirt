package almhirt.serialization

import almhirt.common.Command

trait CommandStringSerializer extends StringSerializingToFixedChannel[Command, Command]
trait CommandBinarySerializer extends BinarySerializingToFixedChannel[Command, Command]