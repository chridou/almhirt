package riftwarp.util

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.http._
import riftwarp._
import almhirt.serialization.SerializationParams

class WarpHttpSerializer[T](riftWarp: RiftWarp)(implicit tag: ClassTag[T]) extends HttpSerializer[T] with HttpDeserializer[T] {

  def serialize(what: T, mediaType: AlmMediaType)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[AlmHttpBody] = 
    serializeInternal(what, mediaType.contentFormat, Map.empty)

  def deserialize(mediaType: AlmMediaType, what: AlmHttpBody)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[T] =
     deserializeInternal(mediaType.contentFormat, what, Map.empty)
     
  private def serializeInternal(what: T, channel: String, options: Map[String, Any]): AlmValidation[AlmHttpBody] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      serialized <- riftWarp.departure(theChannel.channelDescriptor, what, options)
      typedSerialized <- theChannel.httpTransmission match {
        case HttpTransmissionAsBinary ⇒ serialized._1.castTo[Array[Byte]].map(BinaryBody)
        case HttpTransmissionAsText ⇒ serialized._1.castTo[String].map(TextBody)
        case NoHttpTransmission ⇒ UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel.""").failure
      }

    } yield typedSerialized
 
  private def deserializeInternal(channel: String, what: AlmHttpBody, options: Map[String, Any] = Map.empty): AlmValidation[T] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      result <- what match {
        case BinaryBody(bytes) if theChannel.httpTransmission == HttpTransmissionAsBinary ⇒
          riftWarp.arrival(channel, bytes, options).flatMap(_.castTo[T])
        case TextBody(text) if theChannel.httpTransmission == HttpTransmissionAsText ⇒
          riftWarp.arrival(channel, text, options).flatMap(_.castTo[T])
        case _ ⇒
          UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel or the serialized representations do not match("${what.getClass().getSimpleName()}" -> "${theChannel.httpTransmission}").""").failure
      }
    } yield result
}

object WarpHttpSerializer {
  def apply[T](rw: RiftWarp)(implicit tag: ClassTag[T]): WarpHttpSerializer[T] = new WarpHttpSerializer[T](rw)
  def command(rw: RiftWarp): WarpHttpSerializer[Command] = new WarpHttpSerializer[Command](rw)
  def event(rw: RiftWarp): WarpHttpSerializer[Event] = new WarpHttpSerializer[Event](rw)
  def commandResponse(rw: RiftWarp): WarpHttpSerializer[almhirt.tracking.CommandResponse] = new WarpHttpSerializer[almhirt.tracking.CommandResponse](rw)
  def problem(rw: RiftWarp): WarpHttpSerializer[Problem] = new WarpHttpSerializer[Problem](rw)

  def collection[T](rw: RiftWarp)(implicit tagT: ClassTag[T]): HttpSerializer[Seq[T]] with HttpDeserializer[Seq[T]] = 
    new CustomHttpSerializerByLookUp[Seq[T]] with CollectionHttpSerializer[T] with HasRiftWarp {
      val myRiftWarp = rw
      def tag: ClassTag[TT] = tagT
    }
  
  def direct[T: WarpPacker: WarpUnpacker](rw: RiftWarp): HttpSerializer[T] with HttpDeserializer[T] =
    new CustomHttpSerializer[T] with SimpleHttpSerializer[T] with RiftWarpHttpSerializer[T] {
      lazy val packer = implicitly[WarpPacker[T]].success
      lazy val unpacker = implicitly[WarpUnpacker[T]].success
      lazy val riftwarp = rw
    }

  def collectionDirect[T: WarpPacker: WarpUnpacker](rw: RiftWarp): HttpSerializer[Seq[T]] with HttpDeserializer[Seq[T]] = 
    new CustomHttpSerializer[Seq[T]] with  CollectionHttpSerializer[T] with RiftWarpHttpSerializer[Seq[T]] {
      lazy val packer = implicitly[WarpPacker[T]].success
      lazy val unpacker = implicitly[WarpUnpacker[T]].success
      lazy val riftwarp = rw
    }
}