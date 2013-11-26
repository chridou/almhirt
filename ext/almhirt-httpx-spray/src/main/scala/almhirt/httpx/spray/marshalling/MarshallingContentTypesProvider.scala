package almhirt.httpx.spray.marshalling

import spray.http._

trait MarshallingContentTypesProvider[T] {
  def marshallingContentTypes: Seq[ContentType]
}

trait UnmarshallingContentTypesProvider[T] {
  def unmarshallingContentTypes: Seq[ContentType]
}

trait FullContentTypeProvider[T] extends MarshallingContentTypesProvider[T] with UnmarshallingContentTypesProvider[T]

object MarshallingContentTypesProvider {
  def apply[T](contentTypes: ContentType*): MarshallingContentTypesProvider[T] =
    new MarshallingContentTypesProvider[T] {
      val marshallingContentTypes = contentTypes
    }
}

object UnmarshallingContentTypesProvider {
  def apply[T](contentTypes: ContentType*): UnmarshallingContentTypesProvider[T] =
    new UnmarshallingContentTypesProvider[T] {
      val unmarshallingContentTypes = contentTypes
    }
}

object FullContentTypeProvider {
  def apply[T](contentTypesForMarshalling: Seq[ContentType], contentTypesForUnmarshalling: Seq[ContentType]): FullContentTypeProvider[T] =
    new FullContentTypeProvider[T] {
      val marshallingContentTypes = contentTypesForMarshalling
      val unmarshallingContentTypes = contentTypesForUnmarshalling
    }

  def apply[T](marshalling: MarshallingContentTypesProvider[T], unmarshalling: UnmarshallingContentTypesProvider[T]): FullContentTypeProvider[T] =
    new FullContentTypeProvider[T] {
      val marshallingContentTypes = marshalling.marshallingContentTypes
      val unmarshallingContentTypes = unmarshalling.unmarshallingContentTypes
    }

}