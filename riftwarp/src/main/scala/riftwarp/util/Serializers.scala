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
      def serialize(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serialize(channel)(what, typeHint)
      def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serializeAsync(channel)(what, typeHint)
      def serializeBlobSeparating(blobPolicy: BlobSerializationPolicy)(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serializeBlobSeparating(blobPolicy)(channel)(what, typeHint)
      def serializeBlobSeparatingAsync(blobPolicy: BlobSerializationPolicy)(channel: String)(what: TIn, typeHint: Option[String]) = serializer.serializeBlobSeparatingAsync(blobPolicy)(channel)(what, typeHint)
      def deserialize(channel: String)(what: SerializedRepr, typeHint: Option[String]) = deserializer.deserialize(channel)(what, typeHint)
      def deserializeAsync(channel: String)(what: SerializedRepr, typeHint: Option[String]) = deserializer.deserializeAsync(channel)(what, typeHint)
      def deserializeBlobIntegrating(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]) = deserializer.deserializeBlobIntegrating(blobPolicy)(channel)(what, typeHint)
      def deserializeBlobIntegratingAsync(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]): AlmFuture[TOut] = deserializer.deserializeBlobIntegratingAsync(blobPolicy)(channel)(what, typeHint)
    }
  }
}