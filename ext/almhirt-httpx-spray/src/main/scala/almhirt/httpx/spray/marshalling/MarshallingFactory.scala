package almhirt.httpx.spray.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import almhirt.http._
import almhirt.httpx.spray._
import spray.http.HttpEntity.{ NonEmpty, Empty }

trait MarshallerFactory[T] { self =>
  import Helper._
  import spray.util._

  def marshaller(
    serializer: HttpSerializer[T],
    contentTypes: ContentType*): Marshaller[T] = {
    Marshaller.of[T](contentTypes: _*) { (value, contentType, ctx) =>
      val amt = contentType.mediaType.toAlmMediaType
      if (amt.binary) {
        serializer.serialize(value, amt).fold(
          fail => ctx.handleError(fail.escalate),
          succ => ctx.marshalTo(HttpEntity(contentType.withoutDefinedCharset, succ.value.asInstanceOf[Array[Byte]])))
      } else {
        serializer.serialize(value, amt).fold(
          fail => ctx.handleError(fail.escalate),
          succ => ctx.marshalTo(HttpEntity(contentType, succ.value.asInstanceOf[String])))
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
    deserializer: HttpDeserializer[T],
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
              val amt = contentType.mediaType.toAlmMediaType
              if (amt.binary) {
                deserializer.deserialize(amt, BinaryBody(httpData.toByteArray)).fold(
                  fail => Left(MalformedContent(fail.toString, None)),
                  succ => Right(succ))
              } else {
                deserializer.deserialize(amt, TextBody(httpData.asString)).fold(
                  fail => Left(MalformedContent(fail.toString, None)),
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


