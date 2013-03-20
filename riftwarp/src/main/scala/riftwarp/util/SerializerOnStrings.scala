package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

class RiftSerializerOnString[TIn <: AnyRef](channel: RiftChannel, riftWarp: RiftWarp, blobStorage: Option[BlobStorage { type TBlobId = JUUID }], minBlobSize: Int)(implicit support: HasExecutionContext with CanCreateUuid) extends Serializer[TIn] {
  type SerializedRepr = String

  private val serializeWithRiftWarp: AnyRef => AlmValidation[String] =
    blobStorage match {
      case Some(bs) =>
        def blobDivert(data: Array[Byte], ident: RiftBlobIdentifier): AlmValidation[RiftBlob] =
          if (data.length < minBlobSize)
            RiftBlobArrayValue(data).success
          else
            bs.storeBlob(support.getUuid, data).map(id => RiftBlobRefByUuid(id))
        (what: AnyRef) => riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(channel, None)(what).map(_.manifestation)
        case None =>
        (what: AnyRef) => riftWarp.prepareForWarp[DimensionString](channel, None)(what).map(_.manifestation)
    }

  override def serialize(what: TIn): AlmValidation[(String, String, SerializedRepr)] =
    serializeWithRiftWarp(what).map((channel.channelType, RiftDescriptor(what.getClass()).toParsableString(), _))

  override def serializeAsync(what: TIn): AlmFuture[(String, String, SerializedRepr)] =
    AlmFuture { serialize(what) }

}

class RiftDeserializerFromStrings[TOut <: AnyRef](riftWarp: RiftWarp, blobStorage: Option[BlobStorage { type TBlobId = JUUID }])(implicit support: HasExecutionContext, tag: ClassTag[TOut]) extends Deserializer[TOut] {
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

  override def deserialize(what: String, channel: String, typeIdent: String): AlmValidation[TOut] =
    for {
      riftChannel <- riftWarp.channels.getChannel(channel)
      deserialized <- deserializeWithRiftWarp(what, riftChannel)
    } yield deserialized

  override def deserializeAsync(what: SerializedRepr, channel: String, typeIdent: String): AlmFuture[TOut] =
    AlmFuture { deserialize(what, channel, typeIdent) }
}