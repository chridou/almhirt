package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.http._

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

  /** Creates a function of type [[riftwarp.HttpResponseWorkflow[T]]] that can serialize an AnyRef and create a response based on the result.
   * 
   * The workflow(the returned function):
   * 1) Try to serialize the given data
   * 2) In case of success: Call onSuccess which ends the workflow.
   * 3) In case of a problem: Call reportProblem. Do some logging etc
   * 4) Call launderProblem to map the problem and create a HTTP status code
   * 5) Try to deserialize the problem from step 3.
   * 6) If the problem could be serialized, call onFailure given the serialized problem and the HTTP status code. The workflow ends.
   * 7) If serializing the problem fails, call onProblem with the problem caused by serializing the problem from step 3
   * 8) Call onProblem with a text/plain response created by calling toString on the problem of step 3. The workflow ends.
   * 
   * @Tparams
   * TStringDimension The target [[riftwarp.RiftDimension]] that manifests as a String
   * TResult The type of the result returned by onSuccess and onFailure. This can also be Unit in case you call some kind of callback for responding
   * 
   * @params 
   * onSuccess A Function that is passed a [[riftwarp.RiftHttpStringResponse]]. It returnes a result of type TResult which may be Unit. A call of this function ends the workflow
   * launderProblem Takes a problem and can transform it to another problem(for security reasons) and returns the appropriate HTTP-Status
   * onFailure Creates the response based on the given HTTP status code and the given [[riftwarp.RiftHttpStringResponse]]
   * reportProblem Anytime a problem occures this function gets called with the problem. Useful for logging etc.
   * 
   * @return Usually the a response that can be further processed.  
   */
  def createStringResponseWorkflow[TStringDimension <: RiftStringBasedDimension, TResult](onSuccess: RiftHttpStringResponse => TResult)(launderProblem: Problem => (Problem, HttpError))(onFailure: (HttpError, RiftHttpStringResponse) => TResult)(reportProblem: Problem => Unit)(implicit riftWarp: RiftWarp, mDim: Manifest[TStringDimension]): HttpResponseWorkflow[TResult] = {
    def handleObjectSerializationFailure(originalProblem: Problem, channel: RiftChannel): TResult = {
      reportProblem(originalProblem)
      val (launderedProblem, statusCode) = launderProblem(originalProblem)
      createStringResponse[TStringDimension](channel, None)(launderedProblem)(riftWarp, mDim).fold(
        prob => {
          reportProblem(prob)
          onFailure(statusCode, RiftHttpStringResponse(DimensionString(launderedProblem.toString), "text/plain"))
        },
        rsp => onFailure(statusCode, rsp))
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