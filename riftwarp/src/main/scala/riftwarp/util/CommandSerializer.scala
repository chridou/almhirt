package riftwarp.util

import almhirt.common._
import riftwarp.RiftWarp
import almhirt.serialization.CommandStringSerializer

object RiftCommandStringSerializer {
  def apply(riftWarp: RiftWarp): CommandStringSerializer = {
    val innerSerializer = riftwarp.util.Serializers.createSpecificForStrings[Command](riftWarp)
    new CommandStringSerializer {
      def serialize(channel: String)(what: Command, options: Map[String, Any] = Map.empty): AlmValidation[(String, Option[String])] =
        innerSerializer.serialize(channel)(what, options)
      def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty): AlmValidation[Command] =
        innerSerializer.deserialize(channel)(what, options)
    }
  }
}