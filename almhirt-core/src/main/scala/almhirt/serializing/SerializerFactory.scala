package almhirt.serializing

import almhirt.common._
import almhirt.serialization._
import almhirt.core.Almhirt
import almhirt.core.Event
import almhirt.domain.DomainEvent

trait EventToStringSerializer extends StringSerializing[Event, Event]
trait EventToBinarySerializer extends BinarySerializing[Event, Event]
trait DomainEventToStringSerializer extends StringSerializing[DomainEvent, DomainEvent]
trait DomainEventToBinarySerializer extends BinarySerializing[DomainEvent, DomainEvent]



trait EventToStringSerializerFactory {
  def createSerializer: AlmValidation[EventToStringSerializer]
}

trait EventToBinarySerializerFactory {
  def createSerializer: AlmValidation[EventToBinarySerializer]
}

trait DomainEventToStringSerializerFactory {
  def createSerializer: AlmValidation[DomainEventToStringSerializer]
}

trait DomainEventToBinarySerializerFactory {
  def createSerializer: AlmValidation[DomainEventToBinarySerializer]
}