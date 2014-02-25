package riftwarpx.http.spray

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._

class WarpPackageWireSerializer(dematerializers: Dematerializers) extends CanSerializeToWire[WarpPackage] {
  def this(riftwarp: RiftWarp) = this(riftwarp.dematerializers)

  override def serialize(channel: String)(what: WarpPackage, options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      serialized <- dematerializers.dematerialize(theChannel.channelDescriptor, what, options)
      typedSerialized <- theChannel.wireTransmission match {
        case WireTransmissionAsBinary => serialized.castTo[Array[Byte]].map(BinaryWire)
        case WireTransmissionAsText => serialized.castTo[String].map(TextWire)
        case NoWireTransmission => UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel.""").failure
      }
    } yield (typedSerialized, None)
}

class WarpPackageWireDeserializer(rematerializers: Rematerializers) extends CanDeserializeFromWire[WarpPackage] {
  def this(riftwarp: RiftWarp) = this(riftwarp.rematerializers)

  override def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      result <- what match {
        case BinaryWire(bytes) if theChannel.wireTransmission == WireTransmissionAsBinary =>
          rematerializers.rematerializeTyped[Array[Byte]](channel, bytes, Map.empty)
        case TextWire(text) if theChannel.wireTransmission == WireTransmissionAsText =>
          rematerializers.rematerializeTyped[String](channel, text, Map.empty)
        case _ =>
          SerializationProblem(s""""$channel" is neither a binary nor a text channel or the serialized representations do not match("${what.getClass().getSimpleName()}" -> "${theChannel.wireTransmission}").""").failure
      }
    } yield result
}

class WarpPackageWireSerializing(dematerializers: Dematerializers, rematerializers: Rematerializers) extends WireSerializer[WarpPackage] {
  def this(riftwarp: RiftWarp) = this(riftwarp.dematerializers, riftwarp.rematerializers)

  private val serializer = new WarpPackageWireSerializer(dematerializers)
  private val deserializer = new WarpPackageWireDeserializer(rematerializers)
  
  override def serialize(channel: String)(what: WarpPackage, options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])] =
  	serializer.serialize(channel)(what, options)
  	
  override def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    deserializer.deserialize(channel)(what, options)
    
}