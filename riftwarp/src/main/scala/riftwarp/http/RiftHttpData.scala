package riftwarp.http

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

sealed trait RiftHttpData { def contentType: HttpContentType }
case object RiftHttpNoContentData extends RiftHttpData { val contentType = HttpNoContentContentType }

sealed trait RiftHttpDataWithContent extends RiftHttpData { def contentType: HttpContentTypeWithChannel }
case class RiftHttpStringData(contentType: HttpContentTypeWithChannel, content: String) extends RiftHttpDataWithContent
case class RiftHttpBinaryData(contentType: HttpContentTypeWithChannel, content: Array[Byte]) extends RiftHttpDataWithContent

object RiftHttpData {
  implicit class RiftHttpDataOps(data: RiftHttpData) {
    def toRiftDimension(): AlmValidation[RiftDimension] =
      data match {
        case RiftHttpStringData(_, content) => DimensionString(content).success
        case RiftHttpBinaryData(_, content) => DimensionBinary(content).success
        case RiftHttpNoContentData => OperationNotSupportedProblem("'toRiftDimension' not allowed for RiftHttpNoContentData").failure
      }

    def getContentType(): HttpContentType =
      data match {
        case RiftHttpStringData(contentType, _) => contentType
        case RiftHttpBinaryData(contentType, _) => contentType
        case RiftHttpNoContentData => HttpNoContentContentType
      }
  }
}