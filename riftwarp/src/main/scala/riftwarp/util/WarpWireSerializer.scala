package riftwarp.util

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

class WarpWireSerializer[-TIn, +TOut](riftWarp: RiftWarp)(implicit tag: ClassTag[TOut]) extends WireSerializer[TIn, TOut] {
  private def serializeWithRiftWarp(what: TIn, channel: String, options: Map[String, Any]): AlmValidation[(WireRepresentation, WarpDescriptor)] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      serialized <- riftWarp.departure(theChannel.channelDescriptor, what, options)
      typedSerialized <- theChannel.wireTransmission match {
        case WireTransmissionAsBinary => serialized._1.castTo[Array[Byte]].map(BinaryWire)
        case WireTransmissionAsText => serialized._1.castTo[String].map(TextWire)
        case NoWireTransmission => UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel.""").failure
      }

    } yield (typedSerialized, serialized._2)

  override def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])] =
    serializeWithRiftWarp(what, channel, options).map(x => (x._1, Some(x._2.toParsableString())))

  override def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[TOut] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      result <- what match {
        case BinaryWire(bytes) if theChannel.wireTransmission == WireTransmissionAsBinary =>
          riftWarp.arrival(channel, bytes, options).flatMap(_.castTo[TOut])
        case TextWire(text) if theChannel.wireTransmission == WireTransmissionAsText =>
          riftWarp.arrival(channel, text, options).flatMap(_.castTo[TOut])
        case _ =>
          UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel or the serialized representation do not match("${what.getClass().getSimpleName()}" -> "${theChannel.wireTransmission}").""").failure
      }
    } yield result
}