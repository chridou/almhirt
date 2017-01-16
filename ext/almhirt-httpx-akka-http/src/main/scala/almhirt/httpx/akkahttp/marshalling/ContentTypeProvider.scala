package almhirt.httpx.akkahttp.marshalling


import almhirt.httpx.akkahttp._
import almhirt.http._
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes

trait MarshallingContentTypeProvider[T] {
  def marshallingContentType: ContentType
}

trait UnmarshallingContentTypesProvider[T] {
  def unmarshallingContentTypes: Seq[ContentType]
}

trait FullContentTypeProvider[T] extends MarshallingContentTypeProvider[T] with UnmarshallingContentTypesProvider[T]

object MarshallingContentTypeProvider {
  def apply[T](contentType: ContentType): MarshallingContentTypeProvider[T] =
    new MarshallingContentTypeProvider[T] {
      val marshallingContentType = contentType
    }

  def apply[T: AlmMediaTypesProvider](implicit defaultEncoding: AlmCharacterEncoding): MarshallingContentTypeProvider[T] = {
    val mdt = implicitly[AlmMediaTypesProvider[T]]
    new MarshallingContentTypeProvider[T] {
      val marshallingContentType = mdt.targetMediaType.toAkkaHttpContentType
    }
  }
}

object UnmarshallingContentTypesProvider {
  def apply[T](contentTypes: ContentType*): UnmarshallingContentTypesProvider[T] =
    new UnmarshallingContentTypesProvider[T] {
      val unmarshallingContentTypes = contentTypes
    }

  def apply[T: AlmMediaTypesProvider](implicit defaultEncoding: AlmCharacterEncoding): UnmarshallingContentTypesProvider[T] = {
    val mdt = implicitly[AlmMediaTypesProvider[T]]
    new UnmarshallingContentTypesProvider[T] {
      val unmarshallingContentTypes = mdt.sourceMediaTypes.toAkkaHttpContentTypes
    }
  }
}

object FullContentTypeProvider {
  def empty[T]: FullContentTypeProvider[T] = FullContentTypeProvider[T](ContentTypes.`text/plain(UTF-8)`, Seq.empty)

  def apply[T](contentTypeForMarshalling: ContentType, contentTypesForUnmarshalling: Seq[ContentType]): FullContentTypeProvider[T] =
    new FullContentTypeProvider[T] {
      val marshallingContentType = contentTypeForMarshalling
      val unmarshallingContentTypes = contentTypesForUnmarshalling
    }

  def apply[T](marshalling: MarshallingContentTypeProvider[T], unmarshalling: UnmarshallingContentTypesProvider[T]): FullContentTypeProvider[T] =
    new FullContentTypeProvider[T] {
      val marshallingContentType = marshalling.marshallingContentType
      val unmarshallingContentTypes = unmarshalling.unmarshallingContentTypes
    }

  def apply[T: AlmMediaTypesProvider](implicit defaultEncoding: AlmCharacterEncoding): FullContentTypeProvider[T] = {
    val mdt = implicitly[AlmMediaTypesProvider[T]]
    new FullContentTypeProvider[T] {
      val marshallingContentType = mdt.targetMediaType.toAkkaHttpContentType
      val unmarshallingContentTypes = mdt.sourceMediaTypes.toAkkaHttpContentTypes
    }
  }

}