package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

sealed trait RiftHttpResponse
case class RiftHttpStringResponse(dim: RiftStringBasedDimension, contentType: String) extends RiftHttpResponse
case class RiftHttpBinaryResponse(dim: RiftByteArrayBasedDimension, contentType: String) extends RiftHttpResponse

object RiftWarpHttpFuns {
  def prepareStringResponse[TStringDimension <: RiftStringBasedDimension](channel: RiftChannel, contentExtOverride: Option[String] = None)(what: AnyRef)(implicit riftWarp: RiftWarp, mDim: Manifest[TStringDimension]): AlmValidation[RiftHttpStringResponse] = {
    for {
      contentExt <- option.cata(contentExtOverride)(ext =>
        ext.success,
        option.cata(channel.httpContentTypeExt)(ext =>
          ext.success,
          UnspecifiedProblem("Could determine a content type extension. The channel was '%s'".format(channel.channelType)).failure))
      decomposer <- riftWarp.barracks.getDecomposerForAny(what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[TStringDimension, AnyRef](channel, None)(NoDivertBlobDivert)(riftWarp, mDim)
      dematerialized <- dematerialzeFun(what, decomposer)
    } yield RiftHttpStringResponse(dematerialized, decomposer.typeDescriptor.identifier + "+" + contentExt)
  }

  def prepareBinaryResponse[TStringDimension <: RiftByteArrayBasedDimension](channel: RiftChannel, contentExtOverride: Option[String] = None)(what: AnyRef)(implicit riftWarp: RiftWarp, mDim: Manifest[TStringDimension]): AlmValidation[RiftHttpBinaryResponse] = {
    for {
      contentExt <- option.cata(contentExtOverride)(ext =>
        ext.success,
        option.cata(channel.httpContentTypeExt)(ext =>
          ext.success,
          UnspecifiedProblem("Could determine a content type extension. The channel was '%s'".format(channel.channelType)).failure))
      decomposer <- riftWarp.barracks.getDecomposerForAny(what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[TStringDimension, AnyRef](channel, None)(NoDivertBlobDivert)(riftWarp, mDim)
      dematerialized <- dematerialzeFun(what, decomposer)
    } yield RiftHttpBinaryResponse(dematerialized, decomposer.typeDescriptor.identifier + "+" + contentExt)
  }
  
//  def postProcessPreparedStringResponse
}