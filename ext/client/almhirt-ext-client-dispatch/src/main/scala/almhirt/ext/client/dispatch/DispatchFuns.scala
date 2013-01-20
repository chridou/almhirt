package almhirt.ext.client.dispatch

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.http._
import riftwarp.http.RiftWarpHttpFuns.RiftHttpFunsSettings
import dispatch._
import com.ning.http.client

object DispatchFuns {
  def putDataAndContentType(data: RiftHttpData, req: client.RequestBuilder): AlmValidation[client.RequestBuilder] = {
    data match {
      case RiftHttpDataWithContent(contentType, RiftStringBody(body)) => req.setBody(body).addHeader("Content-Type", contentType.headerValue).success
      case RiftHttpDataWithContent(contentType, RiftBinaryBody(body)) => req.setBody(body).addHeader("Content-Type", contentType.headerValue).success
      case RiftHttpNoContentData => req.success
    }
  }

  def configureRequest[T <: AnyRef](settings: RiftHttpFunsSettings)(what: T, channel: Option[RiftHttpChannel], req: client.RequestBuilder): AlmValidation[client.RequestBuilder] =
    for {
      httpData <- RiftWarpHttpFuns.createHttpData[T](settings)(what, channel)
      reqBuilder <- putDataAndContentType(httpData, req)
    } yield reqBuilder

  def getHttpData(resp: client.Response): AlmValidation[RiftHttpResponse] =
    for {
      contentType <- RiftHttpContentType.parse(resp.getContentType())
      content <- contentType.getChannel.map(channel =>
        channel.httpBodyType match {
          case RiftStringBodyType => RiftHttpDataWithContent(contentType, RiftStringBody(resp.getResponseBody()))
          case RiftBinaryBodyType => RiftHttpDataWithContent(contentType, RiftBinaryBody(resp.getResponseBodyAsBytes()))
        })
      statusCode <- almhirt.http.HttpStatusCode.getCode(resp.getStatusCode())
    } yield RiftHttpResponse(statusCode, content)

  /**
   * Here's where the blocking happens!!!!!
   */
  def awaitResponseData(req: client.RequestBuilder)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[RiftHttpResponse] = {
    AlmFuture {
      val data =
        Http(req)
          .map(resp => getHttpData(resp))
          .onComplete(x => x.fold(exn => ExceptionCaughtProblem("An exception has been caught", cause = Some(CauseIsThrowable(exn))).failure, v => v))
      data()
    }
  }

  def transformResponse[T <: AnyRef](settings: RiftHttpFunsSettings)(response: RiftHttpResponse): AlmFuture[T] = {
    AlmFuture.promise {
      for {
        (statusCode, channel, riftDescriptor, dim) <- response.explode
        recompose <- {
          def getRecomposer(remat: Rematerializer) = settings.riftWarp.barracks.lookUpFromRematerializer[AnyRef](remat, riftDescriptor)
          RiftWarpFuns.getRecomposeFun[AnyRef](channel, dim.getClass().asInstanceOf[Class[_ <: RiftDimension]], None)(getRecomposer)(NoFetchBlobFetch)(manifest[AnyRef], settings.riftWarp)
        }
        recomposed <- recompose(dim)
        retyped <- recomposed match {
          case prob: Problem => prob.failure
          case result => result.castTo[T]
        }
      } yield retyped
    }
  }

  def getResponseResult[T <: AnyRef](settings: RiftHttpFunsSettings, req: client.RequestBuilder)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[T] =
    for {
      respData <- awaitResponseData(req)
      result <- transformResponse[T](settings)(respData)
    } yield result
}