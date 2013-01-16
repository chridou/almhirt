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
  def getContentType(req: HttpRequest[Any]): Option[String] =
    RequestContentType(req)

  def getBodyData(httpBodyType: RiftHttpBodyType, req: HttpRequest[Any]): AlmValidation[RiftHttpBody] = {
    httpBodyType match {
      case RiftStringBodyType => RiftStringBody(new String(Body.bytes(req), "UTF-8")).success
      case RiftBinaryBodyType => RiftBinaryBody(Body.bytes(req)).success
    }
  }

  def createGetHttpDataFromRequest(req: HttpRequest[Any]): () => AlmValidation[RiftHttpData] =
    () => RiftWarpHttpFuns.createHttpDataFromRequest(() => getContentType(req), bodyType => getBodyData(bodyType, req))

  def createResponseFunction(httpResponse: RiftHttpResponse): ResponseFunction[Any] = {
    httpResponse.data match {
      case RiftHttpNoContentData => Status(httpResponse.statusCode.code)
      case RiftHttpDataWithContent(contentType, content) =>
        content match {
          case RiftStringBody(data) =>
            Status(httpResponse.statusCode.code) ~> ResponseString(data) ~> ContentType(contentType.headerValue)
          case RiftBinaryBody(data) =>
            Status(httpResponse.statusCode.code) ~> ResponseBytes(data) ~> ContentType(contentType.headerValue)
        }
    }
  }
    
  def processRequest[TReq <: AnyRef, TResp <: AnyRef](settings: RiftWarpHttpFuns.RiftHttpFunsSettings, okStatus: HttpSuccess, computeResponse: TReq => AlmValidation[Option[TResp]], req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any])(implicit mReq: Manifest[TReq]) {
    val resp = RiftWarpHttpFuns.processRequest[TReq, TResp](settings, createGetHttpDataFromRequest(req), okStatus, computeResponse)
    val respFun = createResponseFunction(resp)
    responder.respond(respFun)
  }
  
  def processRequestRespondOnFuture[TReq <: AnyRef, TResp <: AnyRef](settings: RiftWarpHttpFuns.RiftHttpFunsSettings, okStatus: HttpSuccess, computeResponse: TReq => AlmFuture[Option[TResp]], req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any])(implicit mReq: Manifest[TReq], hasExecutor: HasExecutionContext) {
    val resp = RiftWarpHttpFuns.processRequestRespondOnFuture[TReq, TResp](settings, createGetHttpDataFromRequest(req), okStatus, computeResponse)
    RiftWarpHttpFuns.futureResponder(settings, http => responder.respond(createResponseFunction(http)), resp)
  }
  
}