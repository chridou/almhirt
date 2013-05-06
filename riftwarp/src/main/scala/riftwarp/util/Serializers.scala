package riftwarp.util

import almhirt.common.HasExecutionContext
import scala.reflect.ClassTag
import almhirt.serialization.StringSerializing
import riftwarp.RiftWarp

object Serializers {
  def createForStrings[TIn, TOut](riftWarp: RiftWarp)(implicit support: HasExecutionContext, tag: ClassTag[TOut]): StringSerializing[TIn, TOut] = {
    val serializer = new WarpSerializerToString[TIn](riftWarp)
    val deserializer = new WarpDeserializerFromString[TOut](riftWarp)
    new StringSerializing[TIn, TOut] {
      def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, options)
      def serializeAsync(channel: String)(what: TIn, options: Map[String, Any] = Map.empty) = serializer.serializeAsync(channel)(what, options)
      def deserialize(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty) = deserializer.deserialize(channel)(what, options)
      def deserializeAsync(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty) = deserializer.deserializeAsync(channel)(what, options)
    }
  }
  
  def createSpecificForStrings[T](riftWarp: RiftWarp)(implicit support: HasExecutionContext, tag: ClassTag[T]): StringSerializing[T, T] = 
    createForStrings[T, T](riftWarp)
}