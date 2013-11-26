package almhirt.httpx.spray.marshalling

import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import almhirt.serialization._
import spray.http.HttpEntity.{ NonEmpty, Empty }

trait ContentTypeBoundMarshallerFactory[T] {
  import Helper._
  import spray.util._

  def baseMarshallerFactory: MarshallerFactory[T]
  def marshallingContentTypes: Seq[ContentType]

  def marshaller(implicit serializer: CanSerializeToWire[T]): AlmValidation[Marshaller[T]] =
    baseMarshallerFactory.marshaller(serializer, marshallingContentTypes: _*)
}

object ContentTypeBoundMarshallerFactory {
  def create[T: MarshallingContentTypesProvider]: ContentTypeBoundMarshallerFactory[T] = {
    new MarshallerFactory[T] {}.marshalToContentType
  }
  def apply[T: MarshallingContentTypesProvider: UnmarshallingContentTypesProvider: MarshallerFactory]: ContentTypeBoundMarshallerFactory[T] = {
    implicitly[MarshallerFactory[T]].marshalToContentType
  }
}

trait ContentTypeBoundUnmarshallerFactory[T] {
  import Helper._
  import spray.util._

  def baseUnmarshallerFactory: UnmarshallerFactory[T]
  def unmarshallingContentTypes: Seq[ContentType]

  def unmarshaller(implicit deserializer: CanDeserializeFromWire[T]): AlmValidation[Unmarshaller[T]] =
    baseUnmarshallerFactory.unmarshaller(deserializer, unmarshallingContentTypes: _*)
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
  def create[T: MarshallingContentTypesProvider: UnmarshallingContentTypesProvider]: ContentTypeBoundMarshallingFactory[T] = {
    new MarshallingFactory[T] {}.bindToContentType
  }
  def apply[T: MarshallingContentTypesProvider: UnmarshallingContentTypesProvider: MarshallingFactory]: ContentTypeBoundMarshallingFactory[T] = {
    implicitly[MarshallingFactory[T]].bindToContentType
  }
}