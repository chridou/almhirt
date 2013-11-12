package almhirt.serialization

import almhirt.common.Event

trait EventStringSerializer extends StringBasedSerializer[Event, Event]
trait EventBinarySerializer extends BinaryBasedSerializer[Event, Event]