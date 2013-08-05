package riftwarp.util

import scala.reflect.ClassTag
import almhirt.serialization.StringBasedSerializer
import riftwarp.RiftWarp

object Serializers {
  def createForStrings[TIn, TOut](riftWarp: RiftWarp)(implicit tag: ClassTag[TOut]): StringBasedSerializer[TIn, TOut] = {
    val serializer = new WarpSerializerToString[TIn](riftWarp)
    val deserializer = new WarpDeserializerFromString[TOut](riftWarp)
    new StringBasedSerializer[TIn, TOut] {
      def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, options)
      def deserialize(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty) = deserializer.deserialize(channel)(what, options)
    }
  }
  
  def createSpecificForStrings[T](riftWarp: RiftWarp)(implicit tag: ClassTag[T]): StringBasedSerializer[T, T] = 
    createForStrings[T, T](riftWarp)
}