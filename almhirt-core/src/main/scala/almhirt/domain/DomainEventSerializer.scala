package almhirt.domain

import almhirt.serialization._

trait DomainEventStringSerializer extends StringSerializing[DomainEvent, DomainEvent]
trait DomainEventBinarySerializer extends BinarySerializing[DomainEvent, DomainEvent]