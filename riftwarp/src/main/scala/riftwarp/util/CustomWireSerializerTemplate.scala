package riftwarp.util

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

trait CustomWireSerializerTemplate[T] extends WireSerializer[T] {
  type TT

  protected def packInner(what: TT): AlmValidation[WarpPackage]
  protected def unpackInner(what: WarpPackage): AlmValidation[TT]
  protected def packOuter(in: T): AlmValidation[WarpPackage]  
  protected def unpackOuter(out: WarpPackage): AlmValidation[T]  
  protected def getDematerializer(channel: WarpChannel): AlmValidation[Dematerializer[Any]]
  protected def getStringRematerializer(channel: String): AlmValidation[Rematerializer[String]]
  protected def getBinaryRematerializer(channel: String): AlmValidation[Rematerializer[Array[Byte]]]
  
  protected def serializeInternal(what: T, channel: String, pack: T => AlmValidation[WarpPackage]): AlmValidation[WireRepresentation] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      dematerializer <- getDematerializer(theChannel)
      serialized <- pack(what).map(wc => dematerializer.dematerialize(wc, Map.empty))
      typedSerialized <- theChannel.wireTransmission match {
        case WireTransmissionAsBinary => serialized.castTo[Array[Byte]].map(BinaryWire)
        case WireTransmissionAsText => serialized.castTo[String].map(TextWire)
        case NoWireTransmission => UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel.""").failure
      }

    } yield typedSerialized

  protected def deserializeInternal(channel: String)(what: WireRepresentation, unpack: WarpPackage => AlmValidation[T]): AlmValidation[T] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      rematerialized <- what match {
        case BinaryWire(bytes) if theChannel.wireTransmission == WireTransmissionAsBinary =>
          getBinaryRematerializer(theChannel.channelDescriptor).flatMap(_(bytes))
        case TextWire(text) if theChannel.wireTransmission == WireTransmissionAsText =>
          getStringRematerializer(theChannel.channelDescriptor).flatMap(_(text))
        case _ =>
          UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel or the serialized representation do not match("${what.getClass().getSimpleName()}" -> "${theChannel.wireTransmission}").""").failure
      }
      unpacked <- unpack(rematerialized)
    } yield unpacked

  override def serialize(channel: String)(what: T, options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])] =
    serializeInternal(what, channel, packOuter).map(x => (x, None))

  override def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[T] =
    deserializeInternal(channel)(what, unpackOuter)
}


trait SimpleWireSerializer[T] { self : CustomWireSerializerTemplate[T] =>
  type TT = T

  override protected def packOuter(in: T): AlmValidation[WarpPackage]  = packInner(in)
  override def unpackOuter(out: WarpPackage): AlmValidation[T]= unpackInner(out)
}

