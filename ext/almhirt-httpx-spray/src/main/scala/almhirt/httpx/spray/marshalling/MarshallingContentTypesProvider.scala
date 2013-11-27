package almhirt.httpx.spray.marshalling

import spray.http._
import almhirt.httpx.spray.MediaTypesProvider

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

  def apply[T: MediaTypesProvider]: MarshallingContentTypesProvider[T] = {
    val mdt = implicitly[MediaTypesProvider[T]]
    new MarshallingContentTypesProvider[T] {
      val marshallingContentTypes = mdt.marshallableMediaTypes.map(ContentType(_))
    }
  }
}

object UnmarshallingContentTypesProvider {
  def apply[T](contentTypes: ContentType*): UnmarshallingContentTypesProvider[T] =
    new UnmarshallingContentTypesProvider[T] {
      val unmarshallingContentTypes = contentTypes
    }

  def apply[T: MediaTypesProvider]: UnmarshallingContentTypesProvider[T] = {
    val mdt = implicitly[MediaTypesProvider[T]]
    new UnmarshallingContentTypesProvider[T] {
      val unmarshallingContentTypes = mdt.unmarshallableMediaTypes.map(ContentType(_))
    }
  }
}

object FullContentTypeProvider {
  def empty[T]: FullContentTypeProvider[T] = FullContentTypeProvider[T](Seq.empty, Seq.empty)

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

  def apply[T: MediaTypesProvider]: FullContentTypeProvider[T] = {
    val mdt = implicitly[MediaTypesProvider[T]]
    new FullContentTypeProvider[T] {
      val marshallingContentTypes = mdt.marshallableMediaTypes.map(ContentType(_))
      val unmarshallingContentTypes = mdt.unmarshallableMediaTypes.map(ContentType(_))
    }
  }

}