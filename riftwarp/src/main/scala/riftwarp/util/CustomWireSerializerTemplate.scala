package riftwarp.util

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

trait CustomWireSerializerTemplate[TIn, TOut] extends WireSerializer[TIn, TOut] {
  type TTIn
  type TTOut

  protected def packInner(what: TTIn): AlmValidation[WarpPackage]
  protected def unpackInner(what: WarpPackage): AlmValidation[TTOut]
  protected def packOuter(in: TIn): AlmValidation[WarpPackage]  
  protected def unpackOuter(out: WarpPackage): AlmValidation[TOut]  
  protected def getDematerializer(channel: WarpChannel): AlmValidation[Dematerializer[Any]]
  protected def getStringRematerializer(channel: String): AlmValidation[Rematerializer[String]]
  protected def getBinaryRematerializer(channel: String): AlmValidation[Rematerializer[Array[Byte]]]
  
  protected def serializeInternal(what: TIn, channel: String, pack: TIn => AlmValidation[WarpPackage]): AlmValidation[WireRepresentation] =
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

  protected def deserializeInternal(channel: String)(what: WireRepresentation, unpack: WarpPackage => AlmValidation[TOut]): AlmValidation[TOut] =
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

  override def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])] =
    serializeInternal(what, channel, packOuter).map(x => (x, None))

  override def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[TOut] =
    deserializeInternal(channel)(what, unpackOuter)
}


trait SimpleWireSerializer[TIn, TOut] { self : CustomWireSerializerTemplate[TIn, TOut] =>
  type TTIn = TIn
  type TTOut = TOut

  override protected def packOuter(in: TIn): AlmValidation[WarpPackage]  = packInner(in)
  override def unpackOuter(out: WarpPackage): AlmValidation[TOut]= unpackInner(out)
}

