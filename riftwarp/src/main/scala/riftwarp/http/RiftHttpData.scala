package riftwarp.http

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import almhirt.http.HttpStatusCode

sealed trait RiftHttpData { def contentType: RiftHttpContentType }
case object RiftHttpNoContentData extends RiftHttpData { val contentType = RiftHttpNoContentContentType }
case class RiftHttpDataWithContent(contentType: RiftHttpContentTypeWithChannel, body: RiftHttpBody) extends RiftHttpData

object RiftHttpData {
  implicit class RiftHttpDataOps1(data: RiftHttpData) {
    def toRiftDimension(): AlmValidation[RiftHttpDimension] =
      data match {
        case RiftHttpDataWithContent(_, RiftStringBody(content)) => DimensionString(content).success
        case RiftHttpDataWithContent(_, RiftBinaryBody(content)) => DimensionBinary(content).success
        case RiftHttpNoContentData => OperationNotSupportedProblem("'toRiftDimension' not allowed for RiftHttpNoContentData").failure
      }
    def getContentType(): RiftHttpContentType =
      data match {
        case RiftHttpDataWithContent(contentType, _) => contentType
        case RiftHttpNoContentData => RiftHttpNoContentContentType
      }
  }
}


