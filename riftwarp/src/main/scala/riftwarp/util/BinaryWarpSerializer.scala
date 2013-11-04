package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

class ToBinaryWarpSerializer[TIn <: Any](riftWarp: RiftWarp) extends CanSerialize[TIn] {
  type SerializedRepr = Array[Byte]

  private def serializeWithRiftWarp(what: TIn, channel: String, options: Map[String, Any]): AlmValidation[(Array[Byte], WarpDescriptor)] =
    for {
      theChannel <- WarpChannels.getBinaryChannel(channel)
      serialized <- riftWarp.departure(theChannel.channelDescriptor, what, options)
      typedSerialized <- serialized._1.castTo[Array[Byte]]
    } yield (typedSerialized, serialized._2)
    

  override def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(Array[Byte], Option[String])] =
    serializeWithRiftWarp(what, channel, options).map(x => (x._1, Some(x._2.toParsableString())))
}

class WarpDeserializerFromBinary[TOut <: Any](riftWarp: RiftWarp)(implicit tag: ClassTag[TOut]) extends CanDeserialize[TOut] {
  type SerializedRepr = Array[Byte]

  override def deserialize(channel: String)(what: Array[Byte], options: Map[String, Any] = Map.empty): AlmValidation[TOut] =
    riftWarp.arrival(channel, what, options).flatMap(_.castTo[TOut])
}
