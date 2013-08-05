package almhirt.corex.spray.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

trait MarshallingFactory[T] {
  import Helper._
  import spray.util._

  def marshaller(
    stringSerializer: almhirt.serialization.StringBasedSerializer[T, T],
    binarySerializer: almhirt.serialization.BinaryBasedSerializer[T, T],
    contentTypes: ContentType*): AlmValidation[Marshaller[T]] =
    validateMediaTypes(contentTypes.map(_.mediaType)).map { _ =>
      Marshaller.of[T](contentTypes: _*) { (value, contentType, ctx) =>
        val channel = extractChannel(contentType.mediaType)
        if (contentType.mediaType.binary) {
          val (res, _) = binarySerializer.serialize(channel)(value).resultOrEscalate
          ctx.marshalTo(HttpEntity(contentType, res))
        } else {
          val (res, _) = stringSerializer.serialize(channel)(value).resultOrEscalate
          ctx.marshalTo(HttpEntity(contentType, res))
        }
      }
    }

  def unmarshaller(
    stringSerializer: almhirt.serialization.StringBasedSerializer[T, T],
    binarySerializer: almhirt.serialization.BinaryBasedSerializer[T, T],
    contentTypes: ContentType*): AlmValidation[Unmarshaller[T]] =
    validateMediaTypes(contentTypes.map(_.mediaType)).map { _ =>
      Unmarshaller[T](contentTypes: _*) {
        case HttpBody(contentType, buffer) =>
          val channel = extractChannel(contentType.mediaType)
          if (contentType.mediaType.binary) {
            binarySerializer.deserialize(channel)(buffer, Map.empty).resultOrEscalate
          } else {
            stringSerializer.deserialize(channel)(buffer.asString, Map.empty).resultOrEscalate
          }
      }
    }
}