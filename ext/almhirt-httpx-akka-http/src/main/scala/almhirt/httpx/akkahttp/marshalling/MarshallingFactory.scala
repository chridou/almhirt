package almhirt.httpx.akkahttp.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import almhirt.http._
import almhirt.httpx.akkahttp._
import akka.http.scaladsl.model._
import akka.stream.Materializer
import scala.concurrent._
import scala.concurrent.duration._
import akka.util.ByteString

trait MarshallerFactory[T] { self ⇒
  import Helper._

  def marshaller(
    serializer: HttpSerializer[T],
    contentType: ContentType): ToEntityMarshaller[T] = {
    Marshaller.withFixedContentType[T, MessageEntity](contentType) { obj =>
      val amt = contentType.mediaType.toAlmMediaType
      if (amt.binary) {
        serializer.serialize(obj, amt).fold(
          fail => fail.escalate,
          succ => HttpEntity(contentType, succ.value.asInstanceOf[Array[Byte]]))
      } else {
        serializer.serialize(obj, amt).fold(
          fail => fail.escalate,
          succ => HttpEntity(succ.value.asInstanceOf[String]))
      }
    }
  }

  def marshalToContentType(implicit mctProvider: MarshallingContentTypeProvider[T]): ContentTypeBoundMarshallerFactory[T] =
    new ContentTypeBoundMarshallerFactory[T] {
      val baseMarshallerFactory: MarshallerFactory[T] = self
      val marshallingContentType: ContentType = mctProvider.marshallingContentType
    }
}

trait UnmarshallerFactory[T] { self ⇒
  import Helper._

  def unmarshaller(
    deserializer: HttpDeserializer[T],
    contentTypes: Seq[ContentType])(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[T] = {
    val supported = contentTypes.map(ct ⇒ if (ct.mediaType.binary) ct else ContentType(ct.mediaType, () => HttpCharsets.`UTF-8`)).map(ContentTypeRange(_)).toSet.toList

    new Unmarshaller[HttpEntity, T] {
      override def apply(entity: HttpEntity)(implicit executionContext: ExecutionContext, materializer: Materializer) = {
        val amt = entity.contentType.mediaType.toAlmMediaType
        entity.dataBytes.runFold(ByteString.empty) { case (acc, b) => acc ++ b }.map { data =>
          entity.contentType match {
            case binary: ContentType.Binary       => deserializer.deserialize(amt, BinaryBody(data.toArray)).forceResult()
            case nonBinary: ContentType.NonBinary => 
              val encoding = entity.contentType.charsetOption.getOrElse(HttpCharsets.`UTF-8`).nioCharset()
              deserializer.deserialize(amt, TextBody(data.decodeString(encoding))).forceResult()
          }          
        }
      }
    }
  }

  def unmarshalFromContentType(implicit umctProvider: UnmarshallingContentTypesProvider[T]): ContentTypeBoundUnmarshallerFactory[T] =
    new ContentTypeBoundUnmarshallerFactory[T] {
      val baseUnmarshallerFactory: UnmarshallerFactory[T] = self
      val unmarshallingContentTypes: Seq[ContentType] = umctProvider.unmarshallingContentTypes
    }
}

trait MarshallingFactory[T] extends MarshallerFactory[T] with UnmarshallerFactory[T] { self ⇒
  def bindToContentType(implicit mctProvider: MarshallingContentTypeProvider[T], umctProvider: UnmarshallingContentTypesProvider[T]): ContentTypeBoundMarshallingFactory[T] =
    new ContentTypeBoundMarshallingFactory[T] {
      val baseMarshallerFactory: MarshallerFactory[T] = self
      val baseUnmarshallerFactory: UnmarshallerFactory[T] = self
      val marshallingContentType: ContentType = mctProvider.marshallingContentType
      val unmarshallingContentTypes: Seq[ContentType] = umctProvider.unmarshallingContentTypes
    }
}


