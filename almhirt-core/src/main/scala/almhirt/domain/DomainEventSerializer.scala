package almhirt.domain

import almhirt.serialization._

trait DomainEventStringSerializer extends StringSerializingToFixedChannel[DomainEvent, DomainEvent]
trait DomainEventBinarySerializer extends BinarySerializingToFixedChannel[DomainEvent, DomainEvent]