package riftwarp.http

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.http.HttpStatusCode
import riftwarp._
import riftwarp.http._

case class RiftHttpResponse(statusCode: HttpStatusCode, data: RiftHttpData)

object RiftHttpResponse {
  implicit class RiftHttpResponseOps(response: RiftHttpResponse) {
    def explode(): AlmValidation[(HttpStatusCode, RiftHttpChannel, Option[RiftDescriptor], RiftHttpDimension)] =
      response.data match {
        case RiftHttpDataWithoutContent =>
          BadDataProblem(s"No content. Status: ${response.statusCode.code}").failure
        case data @ RiftHttpDataWithContent(contentType, body) =>
          data.toRiftDimension.map(dim =>
            (response.statusCode, contentType.channel, contentType.tryGetRiftDescriptor, dim))
      }
  }
}