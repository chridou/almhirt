package riftwarpx.http.akkahttp

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization.SerializationParams
import almhirt.http._
import riftwarp._

class WarpPackageHttpSerializer(dematerializers: Dematerializers) extends HttpSerializer[WarpPackage] {
  def this(riftwarp: RiftWarp) = this(riftwarp.dematerializers)

  def serialize(what: WarpPackage, mediaType: AlmMediaType)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[AlmHttpBody] =
    for {
      theChannel ← WarpChannels.getChannel(mediaType.contentFormat)
      serialized ← dematerializers.dematerialize(theChannel.channelDescriptor, what, Map.empty)
      typedSerialized ← theChannel.httpTransmission match {
        case HttpTransmissionAsBinary ⇒ serialized.castTo[Array[Byte]].map(BinaryBody)
        case HttpTransmissionAsText ⇒ serialized.castTo[String].map(TextBody)
        case NoHttpTransmission ⇒ UnspecifiedProblem(s""""$theChannel" is neither a binary nor a text channel.""").failure
      }
    } yield typedSerialized
}

class WarpPackageHttpDeserializer(rematerializers: Rematerializers) extends HttpDeserializer[WarpPackage] {
  def this(riftwarp: RiftWarp) = this(riftwarp.rematerializers)

  def deserialize(mediaType: AlmMediaType, what: AlmHttpBody)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[WarpPackage] =
    for {
      theChannel ← WarpChannels.getChannel(mediaType.contentFormat)
      result ← what match {
        case BinaryBody(bytes) if theChannel.httpTransmission == HttpTransmissionAsBinary ⇒
          rematerializers.rematerializeTyped[Array[Byte]](theChannel.channelDescriptor, bytes, Map.empty)
        case TextBody(text) if theChannel.httpTransmission == HttpTransmissionAsText ⇒
          rematerializers.rematerializeTyped[String](theChannel.channelDescriptor, text, Map.empty)
        case _ ⇒
          SerializationProblem(s""""$theChannel" is neither a binary nor a text channel or the serialized representations do not match("${what.getClass().getSimpleName()}" → "${theChannel.httpTransmission}").""").failure
      }
    } yield result
}
