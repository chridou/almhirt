package almhirt.ext.core.riftwarp.serializers

import almhirt.common._
import almhirt.domain._
import riftwarp.RiftWarp

object RiftDomainEventStringSerializer {
  def apply(riftWarp: RiftWarp): DomainEventStringSerializer = {
    val innerSerializer = riftwarp.util.Serializers.createSpecificForStrings[DomainEvent](riftWarp)
    new DomainEventStringSerializer {
      def serialize(channel: String)(what: DomainEvent, options: Map[String, Any] = Map.empty): AlmValidation[(String, Option[String])] =
        innerSerializer.serialize(channel)(what, options)
      def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty): AlmValidation[DomainEvent] =
        innerSerializer.deserialize(channel)(what, options)
    }
  }
}
