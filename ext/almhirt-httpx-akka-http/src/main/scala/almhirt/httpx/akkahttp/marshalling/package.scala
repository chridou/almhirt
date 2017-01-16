package almhirt.httpx.akkahttp

import akka.http.scaladsl.model.ContentType

package object marshalling {
  implicit class ContentTypeOps(self: ContentType) {
    def toMarshallingProvider[T]: MarshallingContentTypeProvider[T] =
      MarshallingContentTypeProvider[T](self)
  }

  implicit class ContentTypeSeqOps(self: Seq[ContentType]) {

    def toUnmarshallingProvider[T]: UnmarshallingContentTypesProvider[T] =
      UnmarshallingContentTypesProvider[T](self: _*)

    def marshallsAndThenUnmarshalsWith[T](forMarshalling: ContentType): FullContentTypeProvider[T] =
      FullContentTypeProvider[T](forMarshalling, self)
  }
}