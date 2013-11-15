package almhirt.corex.spray.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import almhirt.serialization._
import spray.http.HttpEntity.{ NonEmpty, Empty }

trait MarshallingFactory[T] {
  import Helper._
  import spray.util._

  def marshaller(
    serializer: CanSerializeToWire[T],
    contentTypes: ContentType*): AlmValidation[Marshaller[T]] =
    validateMediaTypes(contentTypes.map(_.mediaType)).map { _ =>
      Marshaller.of[T](contentTypes: _*) { (value, contentType, ctx) =>
        val channel = extractChannel(contentType.mediaType)
        if (contentType.mediaType.binary && channel != "json") {
          serializer.serialize(channel)(value).fold(
            fail => ctx.handleError(fail.escalate),
            succ => ctx.marshalTo(HttpEntity(contentType, succ._1.value.asInstanceOf[Array[Byte]])))
        } else {
          serializer.serialize(channel)(value).fold(
            fail => ctx.handleError(fail.escalate),
            succ => ctx.marshalTo(HttpEntity(contentType, succ._1.value.asInstanceOf[String])))
        }
      }
    }

  def unmarshaller(
    deserializer: CanDeserializeFromWire[T],
    contentTypes: ContentType*): AlmValidation[Unmarshaller[T]] = {
    validateMediaTypes(contentTypes.map(_.mediaType)).map { _ =>
      val supported = contentTypes.map(ct => if (ct.mediaType.binary) ct else ContentType(ct.mediaType, HttpCharsets.`UTF-8`)).toSet
      new Unmarshaller[T] {
        override def apply(entity: HttpEntity): Deserialized[T] =
          entity match {
            case NonEmpty(contentType, httpData) =>
              if (!supported.contains(contentType)) {
                val msgSupp = supported.map(_.value).mkString("Expected '", "' or '", "'")
                Left(UnsupportedContentType(s""""${contentType}" is not supported. $msgSupp"""))
              } else {
                val channel = extractChannel(contentType.mediaType)
                if (contentType.mediaType.binary && channel != "json") {
                  deserializer.deserialize(channel)(BinaryWire(httpData.toByteArray)).fold(
                    fail => Left(MalformedContent(fail.message, None)),
                    succ => Right(succ))
                } else {
                  deserializer.deserialize(channel)(TextWire(httpData.asString)).fold(
                    fail => Left(MalformedContent(fail.message, None)),
                    succ => Right(succ))
                }
              }
            case Empty =>
              Left(ContentExpected)
          }
      }
    }
  }
}