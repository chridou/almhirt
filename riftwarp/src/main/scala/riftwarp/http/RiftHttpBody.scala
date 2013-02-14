package riftwarp.http

sealed trait RiftHttpBody { def bodyType: RiftHttpBodyType }
final case class RiftStringBody(data: String) extends RiftHttpBody { val bodyType = RiftStringBodyType }
final case class RiftBinaryBody(data: Array[Byte]) extends RiftHttpBody { val bodyType = RiftBinaryBodyType }

import scalaz.syntax.validation._
import almhirt.common._

object RiftHttpBody {
  implicit class HttpBodyOps(body: RiftHttpBody) {
    def toHttpData(contentType: RiftHttpContentType): AlmValidation[RiftHttpDataWithContent] =
      contentType match {
        case RiftHttpNoContentContentType =>
          ArgumentProblem("Cannot create HttpData from HttpBody when argument content type is 'HttpNoContentContentType'").failure
        case contentTypeWithChannel: RiftHttpContentTypeWithChannel =>
          RiftHttpDataWithContent(contentTypeWithChannel, body).success
      }
  }
}