package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

sealed trait RiftHttpResponse
case class RiftHttpStringResponse(dim: RiftStringBasedDimension, contentType: String) extends RiftHttpResponse
case class RiftHttpBinaryResponse(dim: RiftByteArrayBasedDimension, contentType: String) extends RiftHttpResponse

object RiftWarpHttpFuns {
  private trait ContentTypeContainer { def create(td: TypeDescriptor): String }
  private case class ContentTypeExt(contentExt: String) extends ContentTypeContainer { def create(td: TypeDescriptor) = td.identifier + "+" + contentExt }
  private case class ContentType(contentType: String) extends ContentTypeContainer { def create(td: TypeDescriptor) = contentType }
  private object ContentTypeContainer {
    def apply(channel: RiftChannel, contentExtOverride: Option[String] = None): scalaz.Validation[RiftWarpProblem, ContentTypeContainer] =
      option.cata(contentExtOverride)(
        ext => ContentTypeExt(ext).success,
        option.cata(channel.httpContentTypeExt)(
          ext => ContentTypeExt(ext).success,
          option.cata(channel.httpContentType)(
            ct => ContentType(ct).success,
            RiftWarpProblem("Could determine a content type extension. The channel was '%s'".format(channel.channelType)).failure)))
  }

  def createStringResponse[TStringDimension <: RiftStringBasedDimension](channel: RiftChannel, contentExtOverride: Option[String] = None)(what: AnyRef)(implicit riftWarp: RiftWarp, mDim: Manifest[TStringDimension]): AlmValidation[RiftHttpStringResponse] = {
    for {
      contentExt <- ContentTypeContainer(channel, contentExtOverride)
      decomposer <- riftWarp.barracks.getDecomposerForAny(what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[TStringDimension, AnyRef](channel, None)(NoDivertBlobDivert)(riftWarp, mDim)
      dematerialized <- dematerialzeFun(what, decomposer)
    } yield RiftHttpStringResponse(dematerialized, contentExt.create(decomposer.typeDescriptor))
  }

  def createStringResponseWorkflow[TStringDimension <: RiftStringBasedDimension](onSuccess: RiftHttpStringResponse => Unit)(launderProblem: Problem => Problem)(onFailure: RiftHttpStringResponse => Unit)(onRiftWarpProblem: (RiftWarpProblem) => Unit)(implicit riftWarp: RiftWarp, mDim: Manifest[TStringDimension]): HttpResponseWorkflow = {
    def handleObjectSerializationFailure(prob: Problem, channel: RiftChannel) = {
      val launderedProb =
        prob match {
          case p: RiftWarpProblem =>
            onRiftWarpProblem(p)
            launderProblem(p)
          case p =>
            launderProblem(p)
        }
      createStringResponse[TStringDimension](channel, None)(prob)(riftWarp, mDim).fold(
        prob => (),
        onFailure)
    }

    (channel: RiftChannel) =>
      (what: AnyRef) => {
        createStringResponse[TStringDimension](channel, None)(what)(riftWarp, mDim).fold(
          prob => handleObjectSerializationFailure(prob, channel),
          onSuccess)
      }
  }

  def createBinaryResponse[TBinaryDimension <: RiftByteArrayBasedDimension](channel: RiftChannel, contentExtOverride: Option[String] = None)(what: AnyRef)(implicit riftWarp: RiftWarp, mDim: Manifest[TBinaryDimension]): AlmValidation[RiftHttpBinaryResponse] = {
    for {
      contentExt <- ContentTypeContainer(channel, contentExtOverride)
      decomposer <- riftWarp.barracks.getDecomposerForAny(what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[TBinaryDimension, AnyRef](channel, None)(NoDivertBlobDivert)(riftWarp, mDim)
      dematerialized <- dematerialzeFun(what, decomposer)
    } yield RiftHttpBinaryResponse(dematerialized, contentExt.create(decomposer.typeDescriptor))
  }

}