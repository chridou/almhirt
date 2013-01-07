package almhirt.ext.core.unfiltered

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.syntax.almvalidation._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty.ReceivedMessage
import riftwarp._
import riftwarp.http._

object UnfilteredFuns {
  def getContentType(req: HttpRequest[Any]): AlmValidation[String] =
    RequestContentType(req).noneIsBadData("ContentType")

  def dataByChannel(req: HttpRequest[Any])(channel: RiftChannel): AlmValidation[RiftDimension] = {
    channel match {
      case ch: RiftText => NotSupportedProblem("Channel RiftText").failure
      case ch: RiftMap => NotSupportedProblem("Channel RiftMap").failure
      case ch: RiftJson => DimensionString(new String(Body.bytes(req), "UTF-8")).success
      case ch: RiftBson => NotSupportedProblem("Channel RiftBson").failure
      case ch: RiftXml => DimensionString(new String(Body.bytes(req), "UTF-8")).success
      case ch: RiftMessagePack => NotSupportedProblem("Channel RiftMessagePack").failure
      case ch: RiftProtocolBuffers => NotSupportedProblem("Channel RiftProtocolBuffers").failure
      case x => NotSupportedProblem("Channel '%s'".format(x.getClass())).failure
    }
  }

  def transformContent[TResult <: AnyRef](req: HttpRequest[Any])(implicit mTarget: Manifest[TResult], riftWarp: RiftWarp): AlmValidation[TResult] =
    for {
      contentType <- getContentType(req)
      result <- RiftWarpHttpFuns.transformIncomingContent[TResult](dataByChannel(req))(contentType)
    } yield result

  def createSuccessResponse(successStatus: almhirt.http.HttpSuccess, response: RiftHttpResponse): ResponseFunction[Any] = {
    response match {
      case RiftHttpNoContentResponse => Status(successStatus.code) ~> NoContent
      case RiftHttpStringResponse(dim, contentType) => Status(successStatus.code) ~> ResponseString(dim.manifestation) ~> ContentType(contentType)
      case RiftHttpBinaryResponse(dim, contentType) => Status(successStatus.code) ~> ResponseBytes(dim.manifestation) ~> ContentType(contentType)
    }
  }

  def createErrorResponse(errorStatus: almhirt.http.HttpError, response: RiftHttpResponse): ResponseFunction[Any] = {
    response match {
      case RiftHttpNoContentResponse => Status(errorStatus.code) ~> NoContent
      case RiftHttpStringResponse(dim, contentType) => Status(errorStatus.code) ~> ResponseString(dim.manifestation) ~> ContentType(contentType)
      case RiftHttpBinaryResponse(dim, contentType) => Status(errorStatus.code) ~> ResponseBytes(dim.manifestation) ~> ContentType(contentType)
    }
  }

}