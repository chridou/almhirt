package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

class WarpSerializerToString[TIn <: Any](riftWarp: RiftWarp)(implicit support: HasExecutionContext) extends CanSerialize[TIn] {
  type SerializedRepr = String

  private def serializeWithRiftWarp(what: TIn, channel: String, options: Map[String, Any]): AlmValidation[(String, WarpDescriptor)] =
    riftWarp.departureTyped[String](channel, what, options)

  override def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(String, Option[String])] =
    serializeWithRiftWarp(what, channel, options).map(x => (x._1, Some(x._2.toParsableString())))

  override def serializeAsync(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmFuture[(String, Option[String])] =
    AlmFuture { serialize(channel)(what, options) }
}

class WarpDeserializerFromString[TOut <: Any](riftWarp: RiftWarp)(implicit support: HasExecutionContext, tag: ClassTag[TOut]) extends CanDeserialize[TOut] {
  type SerializedRepr = String

  override def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty): AlmValidation[TOut] =
    riftWarp.arrivalTyped[String, TOut](channel, what, options)

  override def deserializeAsync(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty): AlmFuture[TOut] =
    AlmFuture { deserialize(channel)(what, options) }
}