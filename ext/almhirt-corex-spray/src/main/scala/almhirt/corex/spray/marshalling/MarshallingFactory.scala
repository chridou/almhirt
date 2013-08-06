package almhirt.corex.spray.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._

trait MarshallingFactory[T] {
  import Helper._
  import spray.util._

  def marshaller(
    stringSerializer: Option[almhirt.serialization.StringBasedSerializer[T, T]],
    binarySerializer: Option[almhirt.serialization.BinaryBasedSerializer[T, T]],
    contentTypes: ContentType*): AlmValidation[Marshaller[T]] =
    validateMediaTypes(contentTypes.map(_.mediaType)).map { _ =>
      Marshaller.of[T](contentTypes: _*) { (value, contentType, ctx) =>
        val channel = extractChannel(contentType.mediaType)
        if (contentType.mediaType.binary) {
          binarySerializer match {
            case Some(binSer) =>
              val (res, _) = binSer.serialize(channel)(value).resultOrEscalate
              ctx.marshalTo(HttpEntity(contentType, res))
            case None =>
              throw new IllegalArgumentException("Binary content is not supported")
          }
        } else {
          stringSerializer match {
            case Some(strSer) =>
              val (res, _) = strSer.serialize(channel)(value).resultOrEscalate
              ctx.marshalTo(HttpEntity(ContentType(contentType.mediaType, HttpCharsets.`UTF-8`), res))
            case None =>
              throw new IllegalArgumentException("String content is not supported")
          }
        }
      }
    }

  def unmarshaller(
    stringSerializer: Option[almhirt.serialization.StringBasedSerializer[T, T]],
    binarySerializer: Option[almhirt.serialization.BinaryBasedSerializer[T, T]],
    contentTypes: ContentType*): AlmValidation[Unmarshaller[T]] = {
    validateMediaTypes(contentTypes.map(_.mediaType)).map { _ =>
      val supported = contentTypes.map(ct => if(ct.mediaType.binary) ct else ContentType(ct.mediaType, HttpCharsets.`UTF-8`)).toSet
      new Unmarshaller[T] {
        override def apply(entity: HttpEntity): Deserialized[T] =
          entity match {
            case HttpBody(contentType, buffer) =>
              if (!supported.contains(contentType)) {
                val msgSupp = supported.map(_.value).mkString("Expected '", "' or '", "'")
                Left(UnsupportedContentType(s""""${contentType}" is not supported. $msgSupp"""))
              }
              else {
                val channel = extractChannel(contentType.mediaType)
                  if (contentType.mediaType.binary) {
                    binarySerializer match {
                      case Some(binSer) =>
                        binSer.deserialize(channel)(buffer, Map.empty).fold(
                            fail => Left(MalformedContent(fail.message, None)),
                            succ => Right(succ))
                      case None =>
                        Left(UnsupportedContentType(s"""Binary content("${contentType.value}") is not supported"""))
                    }
                  } else {
                    stringSerializer match {
                      case Some(strSer) =>
                        strSer.deserialize(channel)(buffer.asString, Map.empty).fold(
                            fail => Left(MalformedContent(fail.message, None)),
                            succ => Right(succ))
                      case None =>
                        Left(UnsupportedContentType(s"""String content("${contentType.value}") is not supported"""))
                    }
                  }
              }
            case EmptyEntity =>
              Left(ContentExpected)
          }
      }
    }
  }
}