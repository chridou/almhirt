package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

class WarpSerializerToString[TIn <: AnyRef](riftWarp: RiftWarp)(implicit support: HasExecutionContext) extends CanSerialize[TIn] {
  type SerializedRepr = String

  private def serializeWithRiftWarp(what: TIn, channel: String, options: Map[String, Any]): AlmValidation[String] =
    riftWarp.departureTyped[String](channel, what, options)

  override def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(Option[String], SerializedRepr)] =
    serializeWithRiftWarp(what, channel, options).map(x => (Some(WarpDescriptor(what.getClass()).toParsableString()), x))

  override def serializeAsync(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmFuture[(Option[String], SerializedRepr)] =
    AlmFuture { serialize(channel)(what, options) }
}

class WarpDeserializerFromStrings[TOut <: AnyRef](riftWarp: RiftWarp)(implicit support: HasExecutionContext, tag: ClassTag[TOut]) extends CanDeserialize[TOut] {
  type SerializedRepr = String

  override def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty): AlmValidation[TOut] =
    riftWarp.arrivalTyped[String, TOut](channel, what, options)

  override def deserializeAsync(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty): AlmFuture[TOut] =
    AlmFuture { deserialize(channel)(what, options) }
}