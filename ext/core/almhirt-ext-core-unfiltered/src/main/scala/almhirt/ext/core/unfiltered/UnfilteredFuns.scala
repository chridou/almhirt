package almhirt.ext.core.unfiltered

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.http.HttpError
import riftwarp._
import riftwarp.http._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty.ReceivedMessage

object UnfilteredFuns {
  def getContentType(req: HttpRequest[Any]): AlmValidation[String] =
    RequestContentType(req).noneIsBadData("ContentType")

  def dataByRequestType(reqType: HttpRequestDataType, req: HttpRequest[Any]): AlmValidation[RiftDimension with RiftHttpDimension] = {
    reqType match {
      case StringDataRequest => DimensionString(new String(Body.bytes(req), "UTF-8")).success
      case BinaryDataRequest => DimensionBinary(Body.bytes(req)).success
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
      reqRequestDataType <- RiftWarpHttpFuns.getRequiredRequestDataType(channel)
      data <- dataByRequestType(reqRequestDataType, req)
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

  def withRequest(riftWarp: RiftWarp)(nice: Boolean)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    def respondError(httpError: HttpError, response: RiftHttpResponse) = responder.respond(createErrorResponse(httpError, response))
    def getRequestBody(reqType: HttpRequestDataType) = dataByRequestType(reqType, req)
    def getContentType() = option.cata(RequestContentType(req))(_.success, BadDataProblem("No content type specified").failure)
    def compute(channel: RiftChannel with RiftHttpChannel, typedDescriptor: Option[TypeDescriptor], data: RiftDimension with RiftHttpDimension) {
      sys.error("")
    }
    RiftWarpHttpFuns.withRequest[Unit](riftWarp)(nice)(launderProblem)(reportProblem)(respondError)(getContentType)(getRequestBody)(compute)
  }

}