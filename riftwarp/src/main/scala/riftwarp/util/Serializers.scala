package riftwarp.util

import java.util.{ UUID => JUUID }
import scala.reflect.ClassTag
import almhirt.common._
import almhirt.serialization._
import riftwarp._

object Serializers {
  def createForStrings[TIn <: AnyRef, TOut <: AnyRef](riftWarp: RiftWarp)(implicit support: HasExecutionContext with CanCreateUuid, tag: ClassTag[TOut]): StringSerializing[TIn, TOut] = {
    val serializer = new RiftSerializerOnString[TIn](riftWarp)
    val deserializer = new RiftDeserializerFromStrings[TOut](riftWarp)
    new StringSerializing[TIn, TOut] {
      def serialize(channel: String)(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, typeHint, args)
      def serializeAsync(channel: String)(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = serializer.serializeAsync(channel)(what, typeHint, args)
      def deserialize(channel: String)(what: SerializedRepr, typeHint: Option[String], args: Map[String, Any] = Map.empty) = deserializer.deserialize(channel)(what, typeHint, args)
      def deserializeAsync(channel: String)(what: SerializedRepr, typeHint: Option[String], args: Map[String, Any] = Map.empty) = deserializer.deserializeAsync(channel)(what, typeHint, args)
    }
  }
}