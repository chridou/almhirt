package riftwarp.util

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization.SerializationParams
import almhirt.http._
import riftwarp._

trait CustomHttpSerializerTemplate[T] extends HttpSerializer[T] with HttpDeserializer[T] {
  type TT

  protected def packInner(what: TT): AlmValidation[WarpPackage]
  protected def unpackInner(what: WarpPackage): AlmValidation[TT]
  protected def packOuter(in: T): AlmValidation[WarpPackage]  
  protected def unpackOuter(out: WarpPackage): AlmValidation[T]  
  protected def getDematerializer(channel: WarpChannel): AlmValidation[Dematerializer[Any]]
  protected def getStringRematerializer(channel: String): AlmValidation[Rematerializer[String]]
  protected def getBinaryRematerializer(channel: String): AlmValidation[Rematerializer[Array[Byte]]]
  
  protected def serializeInternal(what: T, channel: String, pack: T ⇒ AlmValidation[WarpPackage]): AlmValidation[AlmHttpBody] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      dematerializer <- getDematerializer(theChannel)
      serialized <- pack(what).map(wc ⇒ dematerializer.dematerialize(wc, Map.empty))
      typedSerialized <- theChannel.httpTransmission match {
        case HttpTransmissionAsBinary ⇒ serialized.castTo[Array[Byte]].map(BinaryBody)
        case HttpTransmissionAsText ⇒ serialized.castTo[String].map(TextBody)
        case NoHttpTransmission ⇒ UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel.""").failure
      }

    } yield typedSerialized

  protected def deserializeInternal(channel: String)(what: AlmHttpBody, unpack: WarpPackage ⇒ AlmValidation[T]): AlmValidation[T] =
    for {
      theChannel <- WarpChannels.getChannel(channel)
      rematerialized <- what match {
        case BinaryBody(bytes) if theChannel.httpTransmission == HttpTransmissionAsBinary ⇒
          getBinaryRematerializer(theChannel.channelDescriptor).flatMap(_(bytes))
        case TextBody(text) if theChannel.httpTransmission == HttpTransmissionAsText ⇒
          getStringRematerializer(theChannel.channelDescriptor).flatMap(_(text))
        case _ ⇒
          UnspecifiedProblem(s""""$channel" is neither a binary nor a text channel or the serialized representation do not match("${what.getClass().getSimpleName()}" -> "${theChannel.httpTransmission}").""").failure
      }
      unpacked <- unpack(rematerialized)
    } yield unpacked

    
  override def serialize(what: T, mediaType: AlmMediaType)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[AlmHttpBody] = 
    serializeInternal(what, mediaType.contentFormat, packOuter)

  override def deserialize(mediaType: AlmMediaType, what: AlmHttpBody)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[T] =
     deserializeInternal(mediaType.contentFormat)(what, unpackOuter)
}


trait SimpleHttpSerializer[T] { self : CustomHttpSerializerTemplate[T] ⇒
  type TT = T

  override protected def packOuter(in: T): AlmValidation[WarpPackage]  = packInner(in)
  override def unpackOuter(out: WarpPackage): AlmValidation[T]= unpackInner(out)
}

