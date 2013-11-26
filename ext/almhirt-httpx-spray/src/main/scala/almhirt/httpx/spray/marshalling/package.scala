package almhirt.httpx.spray

import spray.http._

package object marshalling {
  implicit class ContentTypeSeqOps(self: Seq[ContentType]) {
    def toMarshallingProvider[T]: MarshallingContentTypesProvider[T] =
      MarshallingContentTypesProvider[T](self: _*)
      
    def toUnmarshallingProvider[T]: UnmarshallingContentTypesProvider[T] =
      UnmarshallingContentTypesProvider[T](self: _*)

    def marshallsAndThenUnmarshalsWith[T](forUnmarshalling: Seq[ContentType]): FullContentTypeProvider[T] =
      FullContentTypeProvider[T](self, forUnmarshalling)
  }
}