package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

class RiftSerializerOnString[TIn <: AnyRef](riftWarp: RiftWarp)(implicit support: HasExecutionContext) extends CanSerialize[TIn] {
  type SerializedRepr = String

  private def serializeWithRiftWarp(what: TIn, channel: String, blobPolicy: BlobSerializationPolicy): AlmValidation[(String, Vector[ExtractedBlobReference])] =
    riftWarp.channels.getChannel(channel).flatMap(riftChannel =>
      riftWarp.prepareForWarpWithBlobs[DimensionString](blobPolicy)(riftChannel, None)(what).map(x => (x._1.manifestation, x._2)))

  override def serialize(channel: String)(what: TIn, typeHint: Option[String]): AlmValidation[(Option[String], SerializedRepr)] =
    serializeWithRiftWarp(what, channel, BlobSeparationDisabled).map(x => (Some(RiftDescriptor(what.getClass()).toParsableString()), x._1))

  override def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]): AlmFuture[(Option[String], SerializedRepr)] =
    AlmFuture { serialize(channel)(what, typeHint) }

  override def serializeBlobSeparating(blobPolicy: BlobSerializationPolicy)(channel: String)(what: TIn, typeHint: Option[String]) =
    serializeWithRiftWarp(what, channel, BlobSeparationDisabled).map(x => (Some(RiftDescriptor(what.getClass()).toParsableString()), x._1, x._2))

  override def serializeBlobSeparatingAsync(blobPolicy: BlobSerializationPolicy)(channel: String)(what: TIn, typeHint: Option[String]) =
    AlmFuture { serializeBlobSeparating(blobPolicy)(channel)(what, typeHint) }
}

class RiftDeserializerFromStrings[TOut <: AnyRef](riftWarp: RiftWarp)(implicit support: HasExecutionContext, tag: ClassTag[TOut]) extends CanDeserialize[TOut] {
  type SerializedRepr = String

  override def deserialize(channel: String)(what: String, typeHint: Option[String]): AlmValidation[TOut] =
    for {
      riftChannel <- riftWarp.channels.getChannel(channel)
      deserialized <- riftWarp.receiveFromWarp[DimensionString, TOut](riftChannel, None)(DimensionString(what))
    } yield deserialized

  override def deserializeAsync(channel: String)(what: SerializedRepr, typeIdent: Option[String]): AlmFuture[TOut] =
    AlmFuture { deserialize(channel)(what, typeIdent) }

  override def deserializeBlobIntegrating(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeIdent: Option[String]) =
    for {
      riftChannel <- riftWarp.channels.getChannel(channel)
      deserialized <- riftWarp.receiveFromWarpWithBlobs[DimensionString, TOut](blobPolicy)(riftChannel, None)(DimensionString(what))
    } yield deserialized

  override def deserializeBlobIntegratingAsync(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeIdent: Option[String]): AlmFuture[TOut] =
    AlmFuture { deserializeBlobIntegrating(blobPolicy)(channel)(what, typeIdent) }

}