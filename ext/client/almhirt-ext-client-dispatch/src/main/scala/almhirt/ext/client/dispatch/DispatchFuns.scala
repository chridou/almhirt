package almhirt.ext.client.dispatch

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.http._
import dispatch._
import com.ning.http.client

object DispatchFuns {
  def putDataAndContentType(data: RiftHttpData, req: client.RequestBuilder): AlmValidation[client.RequestBuilder] = {
    data match {
      case RiftHttpStringData(contentType, content) => req.setBody(content).addHeader("Content-Type", contentType.headerValue).success
      case RiftHttpBinaryData(contentType, content) => req.setBody(content).addHeader("Content-Type", contentType.headerValue).success
      case RiftHttpNoContentData => req.success
    }
  }

  def configureRequest(riftWarp: RiftWarp)(nice: Boolean)(channel: RiftHttpChannel, what: AnyRef, req: client.RequestBuilder): AlmValidation[client.RequestBuilder] =
    for {
      httpData <- RiftWarpHttpFuns.createHttpData(riftWarp)(channel, nice, None)(what)
      reqBuilder <- putDataAndContentType(httpData, req)
    } yield reqBuilder

  def getHttpData(resp: client.Response): AlmValidation[(almhirt.http.HttpStatusCode, RiftHttpData)] =
    for {
      contentType <- HttpContentType.parse(resp.getContentType())
      data <- contentType.channel.httpBodyType match {
        case StringBodyType => RiftHttpStringData(contentType, resp.getResponseBody()).success
        case BinaryBodyType => RiftHttpBinaryData(contentType, resp.getResponseBodyAsBytes()).success
      }
      statusCode <- almhirt.http.HttpStatusCode.getCode(resp.getStatusCode())
    } yield (statusCode, data)

  /**
   * Here's where the blocking happens!!!!!
   */
  def awaitResponseData(req: client.RequestBuilder)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[(almhirt.http.HttpStatusCode, RiftHttpData)] = {
    AlmFuture {
      val data = Http(req).map(resp => getHttpData(resp)).either
      sys.error("")
    }
  }
}