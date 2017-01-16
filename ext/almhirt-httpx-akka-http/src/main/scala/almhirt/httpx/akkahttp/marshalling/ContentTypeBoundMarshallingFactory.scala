package almhirt.httpx.akkahttp.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.http._
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import scala.concurrent.ExecutionContext
import akka.stream.Materializer

trait ContentTypeBoundMarshallerFactory[T] {
  import Helper._

  def baseMarshallerFactory: MarshallerFactory[T]
  def marshallingContentType: ContentType

  def marshaller(implicit serializer: HttpSerializer[T]): ToEntityMarshaller[T] =
    baseMarshallerFactory.marshaller(serializer, marshallingContentType)
}

object ContentTypeBoundMarshallerFactory {
  def create[T: MarshallingContentTypeProvider]: ContentTypeBoundMarshallerFactory[T] = {
    new MarshallerFactory[T] {}.marshalToContentType
  }
  def apply[T: MarshallingContentTypeProvider: MarshallerFactory]: ContentTypeBoundMarshallerFactory[T] = {
    implicitly[MarshallerFactory[T]].marshalToContentType
  }
}

trait ContentTypeBoundUnmarshallerFactory[T] {
  import Helper._

  def baseUnmarshallerFactory: UnmarshallerFactory[T]
  def unmarshallingContentTypes: Seq[ContentType]

  def unmarshaller(implicit deserializer: HttpDeserializer[T],executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[T] =
    baseUnmarshallerFactory.unmarshaller(deserializer, unmarshallingContentTypes)
}

object ContentTypeBoundUnmarshallerFactory {
  def create[T: UnmarshallingContentTypesProvider]: ContentTypeBoundUnmarshallerFactory[T] = {
    new UnmarshallerFactory[T] {}.unmarshalFromContentType
  }
  def apply[T: UnmarshallingContentTypesProvider: UnmarshallerFactory]: ContentTypeBoundUnmarshallerFactory[T] = {
    implicitly[UnmarshallerFactory[T]].unmarshalFromContentType
  }
}

trait ContentTypeBoundMarshallingFactory[T] extends ContentTypeBoundMarshallerFactory[T] with ContentTypeBoundUnmarshallerFactory[T]

object ContentTypeBoundMarshallingFactory {
  def create[T: MarshallingContentTypeProvider: UnmarshallingContentTypesProvider]: ContentTypeBoundMarshallingFactory[T] = {
    new MarshallingFactory[T] {}.bindToContentType
  }
  def apply[T: MarshallingContentTypeProvider: UnmarshallingContentTypesProvider: MarshallingFactory]: ContentTypeBoundMarshallingFactory[T] = {
    implicitly[MarshallingFactory[T]].bindToContentType
  }
}