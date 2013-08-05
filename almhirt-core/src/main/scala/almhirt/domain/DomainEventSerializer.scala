package almhirt.domain

import almhirt.serialization._

trait DomainEventStringSerializer extends StringBasedSerializer[DomainEvent, DomainEvent]
trait DomainEventBinarySerializer extends BinaryBasedSerializer[DomainEvent, DomainEvent]