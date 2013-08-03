package almhirt.serialization

import almhirt.common.Event

trait EventStringSerializer extends StringSerializing[Event, Event]
trait EventBinarySerializer extends BinarySerializing[Event, Event]