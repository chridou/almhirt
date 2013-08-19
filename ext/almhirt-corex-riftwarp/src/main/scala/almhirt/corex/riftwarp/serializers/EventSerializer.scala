package almhirt.corex.riftwarp.serializers

import almhirt.common._
import almhirt.serialization.EventStringSerializer
import riftwarp.RiftWarp

class RiftEventStringSerializer {
  def apply(riftWarp: RiftWarp): EventStringSerializer = {
    val innerSerializer = riftwarp.util.Serializers.createSpecificForStrings[Event](riftWarp)
    new EventStringSerializer {
      def serialize(channel: String)(what: Event, options: Map[String, Any] = Map.empty): AlmValidation[(String, Option[String])] =
        innerSerializer.serialize(channel)(what, options)
      def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty): AlmValidation[Event] =
        innerSerializer.deserialize(channel)(what, options)
    }
  }
}
