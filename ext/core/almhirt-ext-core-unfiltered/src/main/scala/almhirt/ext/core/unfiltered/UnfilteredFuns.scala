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
import almhirt.http.HttpSuccess

object UnfilteredFuns {
  def dataByRequestType(reqType: HttpRequestDataType, req: HttpRequest[Any]): AlmValidation[RiftDimension with RiftHttpDimension] = {
    reqType match {
      case StringDataRequest => DimensionString(new String(Body.bytes(req), "UTF-8")).success
      case BinaryDataRequest => DimensionBinary(Body.bytes(req)).success
    }
  }

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

  def respondProblem(riftWarp: RiftWarp)(reportProblem: Problem => Unit)(nice: Boolean)(prob: Problem, errorCode: HttpError, channel: RiftChannel with RiftHttpChannel, responder: unfiltered.Async.Responder[Any]) {
    val resp = RiftWarpHttpFuns.createHttpProblemResponse(riftWarp)(reportProblem)(channel, nice, None)(prob)
    val respFun = createErrorResponse(errorCode, resp)
    responder.respond(respFun)
  }

  def withRequest(riftWarp: RiftWarp)(nice: Boolean)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(compute: (RiftChannel with RiftHttpChannel, Option[TypeDescriptor], RiftDimension with RiftHttpDimension, unfiltered.Async.Responder[Any]) => Unit)(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    def respondError(httpError: HttpError, response: RiftHttpResponse) = responder.respond(createErrorResponse(httpError, response))
    def getRequestBody(reqType: HttpRequestDataType) = dataByRequestType(reqType, req)
    def getContentType() = RequestContentType(req).noneIsBadData("ContentType")
    RiftWarpHttpFuns.withRequest[Unit](riftWarp)(nice)(launderProblem)(reportProblem)(respondError)(getContentType)(getRequestBody)((channel, td, dim) => compute(channel, td, dim, responder))
  }

  def createResponseWorkflow(riftWarp: RiftWarp)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(nice: Boolean): (RiftChannel with RiftHttpChannel, AnyRef, HttpSuccess, unfiltered.Async.Responder[Any]) => Unit = {
    (channel: RiftChannel with RiftHttpChannel, what: AnyRef, successCode: HttpSuccess, responder: unfiltered.Async.Responder[Any]) =>
      {
        def onSuccess(successCode: HttpSuccess, resp: RiftHttpResponse) { responder.respond(createSuccessResponse(successCode, resp)) }
        def onFailure(errorCode: HttpError, resp: RiftHttpResponse) { responder.respond(createErrorResponse(errorCode, resp)) }
        RiftWarpHttpFuns.createResponseWorkflow[Unit](riftWarp)(launderProblem)(reportProblem)(nice)(onSuccess)(onFailure)(channel)(what, successCode)
      }
  }
}