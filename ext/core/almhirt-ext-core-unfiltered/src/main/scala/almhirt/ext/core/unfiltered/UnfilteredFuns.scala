package almhirt.ext.core.unfiltered

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.syntax.almvalidation._
import unfiltered.request._
import unfiltered.netty.ReceivedMessage
import riftwarp._
import riftwarp.http.RiftWarpHttpFuns

object UnfilteredFuns {
  def getContentType(req: HttpRequest[ReceivedMessage]): AlmValidation[String] =
    RequestContentType(req).noneIsBadData("ContentType")
    
  def dataByChannel(req: HttpRequest[ReceivedMessage])(channel: RiftChannel): AlmValidation[RiftDimension] = {
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
  
  def transformContent[TResult <: AnyRef](req: HttpRequest[ReceivedMessage])(implicit mTarget: Manifest[TResult], riftWarp: RiftWarp): AlmValidation[TResult] =
    for {
      contentType <- getContentType(req)
      result <- RiftWarpHttpFuns.transformIncomingContent[TResult](dataByChannel(req))(contentType)
    } yield result
}