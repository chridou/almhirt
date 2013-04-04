package almhirt.ext.core.riftwarp

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.serialization._
import almhirt.core._
import almhirt.serializing._
import almhirt.domain.DomainEvent
import _root_.riftwarp.RiftWarp
import _root_.riftwarp.util.Serializers

class RiftWarpEventToStringSerializerFactory extends EventToStringSerializerFactory {
  override def createSerializer(theAlmhirt: Almhirt): AlmValidation[EventToStringSerializer] =
    theAlmhirt.getService[RiftWarp].map(riftWarp => {
      val serializer = Serializers.createForStrings[Event, Event](riftWarp)(theAlmhirt, implicitly[ClassTag[Event]])
      new EventToStringSerializer {
        def serialize(channel: String)(what: Event, typeHint: Option[String]) = serializer.serialize(channel)(what, typeHint)
        def serializeAsync(channel: String)(what: Event, typeHint: Option[String]) = serializer.serializeAsync(channel)(what, typeHint)
        def serializeBlobSeparating(blobPolicy: BlobSerializationPolicy)(channel: String)(what: Event, typeHint: Option[String]) = serializer.serializeBlobSeparating(blobPolicy)(channel)(what, typeHint)
        def serializeBlobSeparatingAsync(blobPolicy: BlobSerializationPolicy)(channel: String)(what: Event, typeHint: Option[String]) = serializer.serializeBlobSeparatingAsync(blobPolicy)(channel)(what, typeHint)
        def deserialize(channel: String)(what: String, typeHint: Option[String]) = serializer.deserialize(channel)(what, typeHint)
        def deserializeAsync(channel: String)(what: String, typeHint: Option[String]) = serializer.deserializeAsync(channel)(what, typeHint)
        def deserializeBlobIntegrating(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]) = serializer.deserializeBlobIntegrating(blobPolicy)(channel)(what, typeHint)
        def deserializeBlobIntegratingAsync(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]): AlmFuture[Event] = serializer.deserializeBlobIntegratingAsync(blobPolicy)(channel)(what, typeHint)
      }
    })
}

class RiftWarpEventToBinarySerializerFactory extends EventToBinarySerializerFactory {
  override def createSerializer(theAlmhirt: Almhirt): AlmValidation[EventToBinarySerializer] =
    ???
}

class RiftWarpDomainEventToStringSerializerFactory extends DomainEventToStringSerializerFactory {
  override def createSerializer(theAlmhirt: Almhirt): AlmValidation[DomainEventToStringSerializer] =
    theAlmhirt.getService[RiftWarp].map(riftWarp =>
      {
        val serializer = Serializers.createForStrings[DomainEvent, DomainEvent](riftWarp)(theAlmhirt, implicitly[ClassTag[DomainEvent]])
        new DomainEventToStringSerializer {
          def serialize(channel: String)(what: DomainEvent, typeHint: Option[String]) = serializer.serialize(channel)(what, typeHint)
          def serializeAsync(channel: String)(what: DomainEvent, typeHint: Option[String]) = serializer.serializeAsync(channel)(what, typeHint)
          def serializeBlobSeparating(blobPolicy: BlobSerializationPolicy)(channel: String)(what: DomainEvent, typeHint: Option[String]) = serializer.serializeBlobSeparating(blobPolicy)(channel)(what, typeHint)
          def serializeBlobSeparatingAsync(blobPolicy: BlobSerializationPolicy)(channel: String)(what: DomainEvent, typeHint: Option[String]) = serializer.serializeBlobSeparatingAsync(blobPolicy)(channel)(what, typeHint)
          def deserialize(channel: String)(what: String, typeHint: Option[String]) = serializer.deserialize(channel)(what, typeHint)
          def deserializeAsync(channel: String)(what: String, typeHint: Option[String]) = serializer.deserializeAsync(channel)(what, typeHint)
          def deserializeBlobIntegrating(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]) = serializer.deserializeBlobIntegrating(blobPolicy)(channel)(what, typeHint)
          def deserializeBlobIntegratingAsync(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]): AlmFuture[DomainEvent] = serializer.deserializeBlobIntegratingAsync(blobPolicy)(channel)(what, typeHint)
        }

      })
}

class RiftWarpDomainEventToBinarySerializerFactory extends DomainEventToBinarySerializerFactory {
  override def createSerializer(theAlmhirt: Almhirt): AlmValidation[DomainEventToBinarySerializer] =
    ???
}