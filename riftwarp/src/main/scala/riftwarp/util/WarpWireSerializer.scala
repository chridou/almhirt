package riftwarp.util

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

class WarpWireSerializer[T](riftWarp: RiftWarp)(implicit tag: ClassTag[T]) extends WireSerializer[T] {
  private def serializeWithRiftWarp(what: T, channel: String, options: Map[String, Any]): AlmValidation[(WireRepresentation, WarpDescriptor)] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      serialized <- riftWarp.departure(theChannel.channelDescriptor, what, options)
      typedSerialized <- theChannel.wireTransmission match {
        case WireTransmissionAsBinary => serialized._1.castTo[Array[Byte]].map(BinaryWire)
        case WireTransmissionAsText => serialized._1.castTo[String].map(TextWire)
        case NoWireTransmission => UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel.""").failure
      }

    } yield (typedSerialized, serialized._2)

  override def serialize(channel: String)(what: T, options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])] =
    serializeWithRiftWarp(what, channel, options).map(x => (x._1, Some(x._2.toParsableString())))

  override def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[T] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      result <- what match {
        case BinaryWire(bytes) if theChannel.wireTransmission == WireTransmissionAsBinary =>
          riftWarp.arrival(channel, bytes, options).flatMap(_.castTo[T])
        case TextWire(text) if theChannel.wireTransmission == WireTransmissionAsText =>
          riftWarp.arrival(channel, text, options).flatMap(_.castTo[T])
        case _ =>
          UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel or the serialized representation do not match("${what.getClass().getSimpleName()}" -> "${theChannel.wireTransmission}").""").failure
      }
    } yield result
}

object WarpWireSerializer {
  def apply[T](rw: RiftWarp)(implicit tag: ClassTag[T]): WarpWireSerializer[T] = new WarpWireSerializer[T](rw)
  def command(rw: RiftWarp): WarpWireSerializer[Command] = new WarpWireSerializer[Command](rw)
  def event(rw: RiftWarp): WarpWireSerializer[Event] = new WarpWireSerializer[Event](rw)
  def problem(rw: RiftWarp): WarpWireSerializer[Problem] = new WarpWireSerializer[Problem](rw)

  def collection[T](rw: RiftWarp)(implicit tagT: ClassTag[T]): WireSerializer[Seq[T]] = 
    new CustomWireSerializerByLookUp[Seq[T]] with CollectionWireSerializer[T] with HasRiftWarp {
      val myRiftWarp = rw
      def tag: ClassTag[TT] = tagT
    }
  
  def direct[T: WarpPacker: WarpUnpacker](rw: RiftWarp): WireSerializer[T] =
    new CustomWireSerializer[T] with SimpleWireSerializer[T] with RiftWarpWireSerializer[T] {
      lazy val packer = implicitly[WarpPacker[T]].success
      lazy val unpacker = implicitly[WarpUnpacker[T]].success
      lazy val riftwarp = rw
    }

  def collectionDirect[T: WarpPacker: WarpUnpacker](rw: RiftWarp): WireSerializer[Seq[T]] = 
    new CustomWireSerializer[Seq[T]] with  CollectionWireSerializer[T] with RiftWarpWireSerializer[Seq[T]] {
      lazy val packer = implicitly[WarpPacker[T]].success
      lazy val unpacker = implicitly[WarpUnpacker[T]].success
      lazy val riftwarp = rw
    }
}