package riftwarp.util

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._
import scala.reflect.ClassTag

//class WarpSerializerToString[TIn <: Any](riftWarp: RiftWarp) extends CanSerialize[TIn] {
//  type SerializedRepr = String
//
//  private def serializeWithRiftWarp(what: TIn, channel: String, options: Map[String, Any]): AlmValidation[(String, WarpDescriptor)] =
//    for {
//      theChannel <- WarpChannels.getTextChannel(channel)
//      serialized <- riftWarp.departure(theChannel.channelDescriptor, what, options)
//      typedSerialized <- serialized._1.castTo[String]
//    } yield (typedSerialized, serialized._2)
//
//  override def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(String, Option[String])] =
//    serializeWithRiftWarp(what, channel, options).map(x => (x._1, Some(x._2.toParsableString())))
//}
//
//class WarpDeserializerFromString[TOut <: Any](riftWarp: RiftWarp)(implicit tag: ClassTag[TOut]) extends CanDeserialize[TOut] {
//  type SerializedRepr = String
//
//  override def deserialize(channel: String)(what: String, options: Map[String, Any] = Map.empty): AlmValidation[TOut] =
//    riftWarp.arrival(channel, what, options).flatMap(_.castTo[TOut])
//}