package riftwarp.util

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

trait CustomWireSerializer[TIn, TOut] extends WireSerializer[TIn, TOut] {
  type TTIn
  type TTOut
  def packer: WarpPacker[TTIn]
  def unpacker: WarpUnpacker[TTOut]

  def packers: WarpPackers
  def unpackers: WarpUnpackers

  def dematerializers: Dematerializers
  def rematerializers: Rematerializers

  protected def packInner(what: TTIn): AlmValidation[WarpPackage] =
    packer(what)(packers)

  protected def unpackInner(what: WarpPackage): AlmValidation[TTOut] =
    unpacker(what)(unpackers)

  protected def packOuter(in: TIn): AlmValidation[WarpPackage]  
  protected def unpackOuter(out: WarpPackage): AlmValidation[TOut]  
    
  protected def serializeInternal(what: TIn, channel: String, pack: TIn => AlmValidation[WarpPackage]): AlmValidation[WireRepresentation] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      dematerializer <- dematerializers.get(channel)
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
          rematerializers.getTyped[Array[Byte]](theChannel.channelDescriptor).flatMap(_(bytes))
        case TextWire(text) if theChannel.wireTransmission == WireTransmissionAsText =>
          rematerializers.getTyped[String](theChannel.channelDescriptor).flatMap(_(text))
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

trait SimpleWireSerializer[TIn, TOut] { self : CustomWireSerializer[TIn, TOut] =>
  type TTIn = TIn
  type TTOut = TOut

  override protected def packOuter(in: TIn): AlmValidation[WarpPackage]  = packInner(in)
  override def unpackOuter(out: WarpPackage): AlmValidation[TOut]= unpackInner(out)
}

trait FlatWireSerializer[TIn, TOut] { self : CustomWireSerializer[TIn, TOut] =>
  def packers: WarpPackers = WarpPackers.empty
  def unpackers: WarpUnpackers = WarpUnpackers.empty
}

trait RiftWarpWireSerializer[TIn, TOut] { self : CustomWireSerializer[TIn, TOut] =>
  def riftwarp: RiftWarp

  override lazy val packers: WarpPackers = riftwarp.packers
  override lazy val unpackers: WarpUnpackers = riftwarp.unpackers

  override lazy val dematerializers: Dematerializers = riftwarp.dematerializers
  override lazy val rematerializers: Rematerializers = riftwarp.rematerializers
  
}
