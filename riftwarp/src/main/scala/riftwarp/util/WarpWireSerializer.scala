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

object WarpWireSerializer {
  def apply[TIn, TOut](rw: RiftWarp)(implicit tag: ClassTag[TOut]): WarpWireSerializer[TIn, TOut] = new WarpWireSerializer[TIn, TOut](rw)
  def commands(rw: RiftWarp): WarpWireSerializer[Command, Command] = new WarpWireSerializer[Command, Command](rw)
  def events(rw: RiftWarp): WarpWireSerializer[Event, Event] = new WarpWireSerializer[Event, Event](rw)
  def problems(rw: RiftWarp): WarpWireSerializer[Problem, Problem] = new WarpWireSerializer[Problem, Problem](rw)

  def collection[T: WarpPacker: WarpUnpacker](rw: RiftWarp): WireSerializer[Seq[T], Seq[T]] = {
    def packAll(items: Vector[T])(implicit packer: WarpPacker[T]): AlmValidation[WarpCollection] = {
      val mapped = items.map(item => packer.pack(item)(rw.packers).toAgg).sequence
      mapped.map(WarpCollection(_))
    }

    def unpackAll(coll: WarpCollection)(implicit unpacker: WarpUnpacker[T]): AlmValidation[Vector[T]] = {
      val mapped = coll.items.map(item => unpacker.unpack(item)(rw.unpackers).toAgg).sequence
      mapped
    }

    def serializeInternal(what: Seq[T], channel: String): AlmValidation[WireRepresentation] =
      for {
        theChannel <- WarpChannels.getChannel(channel)
        dematerializer <- rw.dematerializers.get(channel)
        serialized <- packAll(what.toVector).map(wc => dematerializer.dematerialize(wc, Map.empty))
        typedSerialized <- theChannel.wireTransmission match {
          case WireTransmissionAsBinary => serialized.castTo[Array[Byte]].map(BinaryWire)
          case WireTransmissionAsText => serialized.castTo[String].map(TextWire)
          case NoWireTransmission => UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel.""").failure
        }

      } yield typedSerialized

    def deserializeInternal(channel: String)(what: WireRepresentation): AlmValidation[Seq[T]] =
      for {
        theChannel <- WarpChannels.getChannel(channel)
        rematerialized <- what match {
          case BinaryWire(bytes) if theChannel.wireTransmission == WireTransmissionAsBinary =>
            rw.rematerializers.getTyped[Array[Byte]](theChannel.channelDescriptor).flatMap(_(bytes))
          case TextWire(text) if theChannel.wireTransmission == WireTransmissionAsText =>
            rw.rematerializers.getTyped[String](theChannel.channelDescriptor).flatMap(_(text))
          case _ =>
            UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel or the serialized representation do not match("${what.getClass().getSimpleName()}" -> "${theChannel.wireTransmission}").""").failure
        }
        warpCollection <- rematerialized.to[WarpCollection]
        unpacked <- unpackAll(warpCollection)
      } yield unpacked

    new WireSerializer[Seq[T], Seq[T]] {
      override def serialize(channel: String)(what: Seq[T], options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])] =
        serializeInternal(what, channel).map(x => (x, None))

      override def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[Seq[T]] =
        deserializeInternal(channel)(what)
    }
  }

}