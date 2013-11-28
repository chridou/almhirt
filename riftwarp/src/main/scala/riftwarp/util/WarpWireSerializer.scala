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
  def command(rw: RiftWarp): WarpWireSerializer[Command, Command] = new WarpWireSerializer[Command, Command](rw)
  def event(rw: RiftWarp): WarpWireSerializer[Event, Event] = new WarpWireSerializer[Event, Event](rw)
  def problem(rw: RiftWarp): WarpWireSerializer[Problem, Problem] = new WarpWireSerializer[Problem, Problem](rw)

  def collection[T](rw: RiftWarp)(implicit tag: ClassTag[T]): WireSerializer[Seq[T], Seq[T]] = 
    new CustomWireSerializerByLookUp[Seq[T], Seq[T]] with CollectionWireSerializer[T, T] with HasRiftWarp {
      val myRiftWarp = rw
      def outTag: ClassTag[TTOut] = tag
    }
  
  def direct[T: WarpPacker: WarpUnpacker](rw: RiftWarp): WireSerializer[T, T] =
    new CustomWireSerializer[T, T] with SimpleWireSerializer[T,T] with RiftWarpWireSerializer[T, T] {
      lazy val packer = implicitly[WarpPacker[T]].success
      lazy val unpacker = implicitly[WarpUnpacker[T]].success
      lazy val riftwarp = rw
    }

  def collectionDirect[T: WarpPacker: WarpUnpacker](rw: RiftWarp): WireSerializer[Seq[T], Seq[T]] = 
    new CustomWireSerializer[Seq[T], Seq[T]] with  CollectionWireSerializer[T, T] with RiftWarpWireSerializer[Seq[T], Seq[T]] {
      lazy val packer = implicitly[WarpPacker[T]].success
      lazy val unpacker = implicitly[WarpUnpacker[T]].success
      lazy val riftwarp = rw
    }
}