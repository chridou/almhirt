package almhirt.httpx.spray.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import almhirt.serialization._
import spray.http.HttpEntity.{ NonEmpty, Empty }

trait MarshallerFactory[T] { self =>
  import Helper._
  import spray.util._

  def marshaller(
    serializer: CanSerializeToWire[T],
    contentTypes: ContentType*): Marshaller[T] = {
    Marshaller.of[T](contentTypes: _*) { (value, contentType, ctx) =>
      val channel = extractChannel(contentType.mediaType)
      if (contentType.mediaType.binary && channel != "json") {
        serializer.serialize(channel)(value).fold(
          fail => ctx.handleError(fail.escalate),
          succ => ctx.marshalTo(HttpEntity(contentType.withoutDefinedCharset, succ._1.value.asInstanceOf[Array[Byte]])))
      } else {
        serializer.serialize(channel)(value).fold(
          fail => ctx.handleError(fail.escalate),
          succ => ctx.marshalTo(HttpEntity(contentType, succ._1.value.asInstanceOf[String])))
      }
    }
  }

  def marshalToContentType(implicit mctProvider: MarshallingContentTypesProvider[T]): ContentTypeBoundMarshallerFactory[T] =
    new ContentTypeBoundMarshallerFactory[T] {
      val baseMarshallerFactory: MarshallerFactory[T] = self
      val marshallingContentTypes: Seq[ContentType] = mctProvider.marshallingContentTypes
    }
}

trait UnmarshallerFactory[T] { self =>
  import Helper._
  import spray.util._

  def unmarshaller(
    deserializer: CanDeserializeFromWire[T],
    contentTypes: ContentType*): Unmarshaller[T] = {
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

  def unmarshalFromContentType(implicit umctProvider: UnmarshallingContentTypesProvider[T]): ContentTypeBoundUnmarshallerFactory[T] =
    new ContentTypeBoundUnmarshallerFactory[T] {
      val baseUnmarshallerFactory: UnmarshallerFactory[T] = self
      val unmarshallingContentTypes: Seq[ContentType] = umctProvider.unmarshallingContentTypes
    }
}

trait MarshallingFactory[T] extends MarshallerFactory[T] with UnmarshallerFactory[T] { self =>
  def bindToContentType(implicit mctProvider: MarshallingContentTypesProvider[T], umctProvider: UnmarshallingContentTypesProvider[T]): ContentTypeBoundMarshallingFactory[T] =
    new ContentTypeBoundMarshallingFactory[T] {
      val baseMarshallerFactory: MarshallerFactory[T] = self
      val baseUnmarshallerFactory: UnmarshallerFactory[T] = self
      val marshallingContentTypes: Seq[ContentType] = mctProvider.marshallingContentTypes
      val unmarshallingContentTypes: Seq[ContentType] = umctProvider.unmarshallingContentTypes
    }
}


