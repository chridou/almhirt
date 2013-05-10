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
      val serializer = Serializers.createForStrings[Event, Event](riftWarp)(implicitly[ClassTag[Event]])
      new EventToStringSerializer {
        def serialize(channel: String)(what: Event, options: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, options)
        def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty) = serializer.deserialize(channel)(what, options)
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
        val serializer = Serializers.createForStrings[DomainEvent, DomainEvent](riftWarp)(implicitly[ClassTag[DomainEvent]])
        new DomainEventToStringSerializer {
          def serialize(channel: String)(what: DomainEvent, options: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, options)
          def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty) = serializer.deserialize(channel)(what, options)
        }

      })
}

class RiftWarpDomainEventToBinarySerializerFactory extends DomainEventToBinarySerializerFactory {
  override def createSerializer(theAlmhirt: Almhirt): AlmValidation[DomainEventToBinarySerializer] =
    ???
}