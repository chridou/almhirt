package almhirt.ext.core.unfiltered

import scalaz.std._
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

  def dataByChannel(channel: RiftChannel with RiftHttpChannel)(req: HttpRequest[Any]): AlmValidation[RiftHttpDimension] = {
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

  def transformContent[TResult <: AnyRef](req: HttpRequest[Any], aChannel: Option[RiftChannel with RiftHttpChannel], aTypeDescriptor: Option[TypeDescriptor])(implicit mResult: Manifest[TResult], riftWarp: RiftWarp): AlmValidation[TResult] =
    for {
      (channel, typeDescriptor) <- (aChannel, aTypeDescriptor) match {
        case (Some(ch), Some(td)) => (ch, Some(td)).success
        case (Some(ch), None) => getContentType(req).flatMap(RiftWarpHttpFuns.extractChannelAndTypeDescriptor(_).map(x => (ch, x._2)))
        case (None, Some(td)) => getContentType(req).flatMap(RiftWarpHttpFuns.extractChannelAndTypeDescriptor(_).map(x => (x._1, Some(td))))
        case _ => getContentType(req).flatMap(RiftWarpHttpFuns.extractChannelAndTypeDescriptor(_))
      }
      data <- dataByChannel(channel)(req)
      result <- {
        val td = option.cata(typeDescriptor)(td => td, TypeDescriptor(mResult.runtimeClass))
        RiftWarpHttpFuns.transformIncomingContent[TResult](channel, Some(td), data)
      }
    } yield result

  def createSuccessResponse(successStatus: almhirt.http.HttpSuccess, response: RiftHttpResponse): ResponseFunction[Any] = {
    response match {
      case RiftHttpNoContentResponse => Status(successStatus.code) ~> NoContent
      case RiftHttpStringResponse(content, contentType) => Status(successStatus.code) ~> ResponseString(content) ~> ContentType(contentType)
      case RiftHttpBinaryResponse(content, contentType) => Status(successStatus.code) ~> ResponseBytes(content) ~> ContentType(contentType)
    }
  }

  def createErrorResponse(errorStatus: almhirt.http.HttpError, response: RiftHttpResponse): ResponseFunction[Any] = {
    response match {
      case RiftHttpNoContentResponse => Status(errorStatus.code) ~> NoContent
      case RiftHttpStringResponse(content, contentType) => Status(errorStatus.code) ~> ResponseString(content) ~> ContentType(contentType)
      case RiftHttpBinaryResponse(content, contentType) => Status(errorStatus.code) ~> ResponseBytes(content) ~> ContentType(contentType)
    }
  }

}