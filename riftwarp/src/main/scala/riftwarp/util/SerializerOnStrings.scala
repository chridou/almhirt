package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

class RiftSerializerOnString[TIn <: AnyRef](riftWarp: RiftWarp)(implicit support: HasExecutionContext) extends CanSerialize[TIn] {
  type SerializedRepr = String

  private def serializeWithRiftWarp(what: TIn, channel: String): AlmValidation[String] =
    riftWarp.channels.getChannel(channel).flatMap(riftChannel =>
      riftWarp.prepareForWarp[DimensionString](riftChannel, None)(what).map(_.manifestation))

  override def serialize(channel: String)(what: TIn, typeHint: Option[String]): AlmValidation[(Option[String], SerializedRepr)] =
    serializeWithRiftWarp(what, channel).map(x => (Some(RiftDescriptor(what.getClass()).toParsableString()), x))

  override def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]): AlmFuture[(Option[String], SerializedRepr)] =
    AlmFuture { serialize(channel)(what, typeHint) }
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
}