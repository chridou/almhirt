package riftwarp.util

import java.util.{ UUID => JUUID }
import scala.reflect.ClassTag
import almhirt.common._
import almhirt.serialization._
import riftwarp._

object Serializers {
  def createForStrings[TIn <: AnyRef, TOut <: AnyRef](riftWarp: RiftWarp)(implicit support: HasExecutionContext with CanCreateUuid, tag: ClassTag[TOut]): CanSerializeAndDeserialize[TIn, TOut] = {
    val serializer = new RiftSerializerOnString[TIn](riftWarp, None)
    val deserializer = new RiftDeserializerFromStrings[TOut](riftWarp, None)
    new CanSerializeAndDeserialize[TIn, TOut] {
      type SerializedRepr = String
      def serialize(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serialize(channel)(what, typeHint)
      def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serializeAsync(channel)(what, typeHint)
      def deserialize(channel: String)(what: SerializedRepr, typeHint: Option[String]) = deserializer.deserialize(channel)(what, typeHint)
      def deserializeAsync(channel: String)(what: SerializedRepr, typeHint: Option[String]) = deserializer.deserializeAsync(channel)(what, typeHint)
    }
  }

  def createForStringsWithBlobs[TIn <: AnyRef, TOut <: AnyRef](riftWarp: RiftWarp, blobStorage: BlobStorage { type TBlobId = JUUID }, minBlobSize: Int)(implicit support: HasExecutionContext with CanCreateUuid, tag: ClassTag[TOut]): CanSerializeAndDeserialize[TIn, TOut] = {
    val serializer = new RiftSerializerOnString[TIn](riftWarp, Some((blobStorage, minBlobSize)))
    val deserializer = new RiftDeserializerFromStrings[TOut](riftWarp, Some(blobStorage))
    new CanSerializeAndDeserialize[TIn, TOut] {
      type SerializedRepr = String
      def serialize(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serialize(channel)(what, typeHint)
      def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serializeAsync(channel)(what, typeHint)
      def deserialize(channel: String)(what: SerializedRepr, typeHint: Option[String]) = deserializer.deserialize(channel)(what, typeHint)
      def deserializeAsync(channel: String)(what: SerializedRepr, typeHint: Option[String]) = deserializer.deserializeAsync(channel)(what, typeHint)
    }
  }
}