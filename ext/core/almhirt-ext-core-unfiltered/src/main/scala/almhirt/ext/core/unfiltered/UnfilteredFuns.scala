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
  def dataByChannel(channel: RiftHttpChannel, req: HttpRequest[Any]): AlmValidation[RiftDimension with RiftHttpDimension] = {
    channel.httpBodyType match {
      case StringBodyType => DimensionString(new String(Body.bytes(req), "UTF-8")).success
      case BinaryBodyType => DimensionBinary(Body.bytes(req)).success
    }
  }

  def createSuccessResponse(successStatus: almhirt.http.HttpSuccess, response: RiftHttpData): ResponseFunction[Any] = {
    response match {
      case RiftHttpNoContentData => Status(successStatus.code) ~> NoContent
      case RiftHttpStringData(contentType, content) => Status(successStatus.code) ~> ResponseString(content) ~> ContentType(contentType.headerValue)
      case RiftHttpBinaryData(contentType, content) => Status(successStatus.code) ~> ResponseBytes(content) ~> ContentType(contentType.headerValue)
    }
  }

  def createErrorResponse(errorStatus: almhirt.http.HttpError, response: RiftHttpData): ResponseFunction[Any] = {
    response match {
      case RiftHttpNoContentData => Status(errorStatus.code) ~> NoContent
      case RiftHttpStringData(contentType, content) => Status(errorStatus.code) ~> ResponseString(content) ~> ContentType(contentType.headerValue)
      case RiftHttpBinaryData(contentType, content) => Status(errorStatus.code) ~> ResponseBytes(content) ~> ContentType(contentType.headerValue)
    }
  }

  def respondProblem(riftWarp: RiftWarp)(reportProblem: Problem => Unit)(nice: Boolean)(prob: Problem, errorCode: HttpError, channel: RiftHttpChannel, responder: unfiltered.Async.Responder[Any]) {
    val resp = RiftWarpHttpFuns.createHttpProblemResponse(riftWarp)(reportProblem)(channel, nice, None)(prob)
    val respFun = createErrorResponse(errorCode, resp)
    responder.respond(respFun)
  }

  def withRequest(riftWarp: RiftWarp)(nice: Boolean)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(compute: (HttpContentType, RiftDimension with RiftHttpDimension, unfiltered.Async.Responder[Any]) => Unit)(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    def respondError(httpError: HttpError, response: RiftHttpData) = responder.respond(createErrorResponse(httpError, response))
    def getRequestBody(channel: RiftHttpChannel) = dataByChannel(channel, req)
    def getContentType() = RequestContentType(req).noneIsBadData("ContentType")
    RiftWarpHttpFuns.withRequest[Unit](riftWarp)(nice)(launderProblem)(reportProblem)(respondError)(getContentType)(getRequestBody)((contentType, dim) => compute(contentType, dim, responder))
  }

  def createResponseWorkflow(riftWarp: RiftWarp)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(nice: Boolean): (RiftHttpChannel, AnyRef, HttpSuccess, unfiltered.Async.Responder[Any]) => Unit = {
    (channel: RiftHttpChannel, what: AnyRef, successCode: HttpSuccess, responder: unfiltered.Async.Responder[Any]) =>
      {
        def onSuccess(successCode: HttpSuccess, resp: RiftHttpData) { responder.respond(createSuccessResponse(successCode, resp)) }
        def onFailure(errorCode: HttpError, resp: RiftHttpData) { responder.respond(createErrorResponse(errorCode, resp)) }
        RiftWarpHttpFuns.createResponseWorkflow[Unit](riftWarp)(launderProblem)(reportProblem)(nice)(onSuccess)(onFailure)(channel)(what, successCode)
      }
  }
}