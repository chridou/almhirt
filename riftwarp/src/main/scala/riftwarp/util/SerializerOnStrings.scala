package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

class RiftSerializerOnString[TIn <: AnyRef](riftWarp: RiftWarp, blobStorage: Option[(BlobStorage { type TBlobId = JUUID }, Int)])(implicit support: HasExecutionContext with CanCreateUuid) extends CanSerialize[TIn] {
  type SerializedRepr = String

  private val serializeWithRiftWarp: (AnyRef, String) => AlmValidation[String] =
    blobStorage match {
      case Some((bs, minBlobSize)) =>
        def blobDivert(data: Array[Byte], ident: RiftBlobIdentifier): AlmValidation[RiftBlob] =
          if (data.length < minBlobSize)
            RiftBlobArrayValue(data).success
          else
            bs.storeBlob(support.getUuid, data).map(id => RiftBlobRefByUuid(id))
        (what: AnyRef, channel: String) =>
          riftWarp.channels.getChannel(channel).flatMap(riftChannel =>
            riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(riftChannel, None)(what).map(_.manifestation))
          case None =>
        (what: AnyRef, channel: String) =>
          riftWarp.channels.getChannel(channel).flatMap(riftChannel =>
            riftWarp.prepareForWarp[DimensionString](riftChannel, None)(what).map(_.manifestation))
    }

  override def serialize(channel: String)(what: TIn, typeHint: Option[String]): AlmValidation[(String, SerializedRepr)] =
    serializeWithRiftWarp(what, channel).map((RiftDescriptor(what.getClass()).toParsableString(), _))

  override def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]): AlmFuture[(String, SerializedRepr)] =
    AlmFuture { serialize(channel)(what, typeHint) }

}

class RiftDeserializerFromStrings[TOut <: AnyRef](riftWarp: RiftWarp, blobStorage: Option[BlobStorage { type TBlobId = JUUID }])(implicit support: HasExecutionContext, tag: ClassTag[TOut]) extends CanDeserialize[TOut] {
  type SerializedRepr = String

  private val deserializeWithRiftWarp: (String, RiftChannel) => AlmValidation[TOut] =
    blobStorage match {
      case Some(bs) =>
        def blobFetch(blob: RiftBlob): AlmValidation[Array[Byte]] =
          blob match {
            case RiftBlobRefByUuid(uuid) =>
              bs.fetchBlob(uuid)
            case x =>
              UnspecifiedProblem(s"Can only recreate blobs from RiftBlobRefByUuid. Got '${x.getClass().getName}'").failure
          }
        (what: String, channel: RiftChannel) => riftWarp.receiveFromWarpWithBlobs[DimensionString, TOut](blobFetch)(channel, None)(DimensionString(what))
        case None =>
        (what: String, channel: RiftChannel) => riftWarp.receiveFromWarp[DimensionString, TOut](channel, None)(DimensionString(what))
    }

  override def deserialize(channel: String)(what: String, typeHint: Option[String]): AlmValidation[TOut] =
    for {
      riftChannel <- riftWarp.channels.getChannel(channel)
      deserialized <- deserializeWithRiftWarp(what, riftChannel)
    } yield deserialized

  override def deserializeAsync(channel: String)(what: SerializedRepr, typeIdent: Option[String]): AlmFuture[TOut] =
    AlmFuture { deserialize(channel)(what, typeIdent) }
}