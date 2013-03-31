package almhirt.serializing

import almhirt.common._
import almhirt.serialization._
import almhirt.core.Almhirt
import almhirt.core.Event
import almhirt.domain.DomainEvent
import almhirt.domain.IsAggregateRoot

trait EventToStringSerializer extends StringSerializing[Event, Event]
trait EventToBinarySerializer extends BinarySerializing[Event, Event]
trait DomainEventToStringSerializer extends StringSerializing[DomainEvent, DomainEvent]
trait DomainEventToBinarySerializer extends BinarySerializing[DomainEvent, DomainEvent]
trait AggregateRootToStringSerializer extends StringSerializing[IsAggregateRoot, IsAggregateRoot]
trait AggregateRootToBinarySerializer extends BinarySerializing[IsAggregateRoot, IsAggregateRoot]



trait EventToStringSerializerFactory {
  def createSerializer(storeBlobsHere: Option[(BlobStorageWithUuidBlobId, Int)], theAlmhirt: Almhirt): AlmValidation[EventToStringSerializer]
}

trait EventToBinarySerializerFactory {
  def createSerializer(storeBlobsHere: Option[(BlobStorageWithUuidBlobId, Int)], theAlmhirt: Almhirt): AlmValidation[EventToBinarySerializer]
}

trait DomainEventToStringSerializerFactory {
  def createSerializer(storeBlobsHere: Option[(BlobStorageWithUuidBlobId, Int)], theAlmhirt: Almhirt): AlmValidation[DomainEventToStringSerializer]
}

trait DomainEventToBinarySerializerFactory {
  def createSerializer(storeBlobsHere: Option[(BlobStorageWithUuidBlobId, Int)], theAlmhirt: Almhirt): AlmValidation[DomainEventToBinarySerializer]
}

trait AggregateRootToStringSerializerFactory {
  def createSerializer(storeBlobsHere: Option[(BlobStorageWithUuidBlobId, Int)], theAlmhirt: Almhirt): AlmValidation[AggregateRootToStringSerializer]
}

trait AggregateRootToBinarySerializerFactory {
  def createSerializer(storeBlobsHere: Option[(BlobStorageWithUuidBlobId, Int)], theAlmhirt: Almhirt): AlmValidation[AggregateRootToBinarySerializer]
}