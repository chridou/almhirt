package almhirt.serialization

import almhirt.common.Event

trait EventStringSerializer extends StringSerializingToFixedChannel[Event, Event]
trait EventBinarySerializer extends BinarySerializingToFixedChannel[Event, Event]