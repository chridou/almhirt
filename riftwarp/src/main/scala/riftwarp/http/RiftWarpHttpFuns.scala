package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.http.HttpError
import riftwarp._
import riftwarp.RiftStringBasedDimension


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

  
  def createHttpResponse(channel: RiftChannel with RiftHttpChannel, nice: Boolean, contentExtOverride: Option[String] = None)(what: AnyRef)(implicit riftWarp: RiftWarp): AlmValidation[RiftHttpResponse] = {
    for {
      contentExt <- ContentTypeContainer(channel, contentExtOverride)
      decomposer <- riftWarp.barracks.getDecomposerForAny(what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[AnyRef](channel, channel.httpDimensionType(nice), None)(NoDivertBlobDivert)(riftWarp)
      dematerialized <- dematerialzeFun(what, decomposer)
      response <-
        dematerialized match {
        case dim : RiftStringBasedDimension => RiftHttpStringResponse(dim.manifestation, contentExt.create(decomposer.typeDescriptor)).success
        case dim : RiftByteArrayBasedDimension => RiftHttpBinaryResponse(dim.manifestation, contentExt.create(decomposer.typeDescriptor)).success
        case x => UnspecifiedProblem("Not a valid HTTP-Dimension: %s".format(x.getClass().getName())).failure
      }
    } yield response
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
   * 8) Call onFailure with a text/plain response created by calling toString on the problem of step 3. The workflow ends.
   * 
   * @Tparams
   * TStringDimension The target [[riftwarp.RiftDimension]] that manifests as a String
   * TResult The type of the result returned by onSuccess and onFailure. This can also be Unit in case you call some kind of callback for responding
   * 
   * @params 
   * nice If possible, create a nice response
   * onSuccess A Function that is passed a [[riftwarp.http.RiftHttpResponse]]. It returns a result of type TResult which may be Unit. A call of this function ends the workflow
   * launderProblem Takes a problem and can transform it to another problem(for security reasons) and returns the appropriate HTTP-Status
   * onFailure Creates the response based on the given HTTP status code and the given [[riftwarp.RiftHttpResponse]]
   * reportProblem Anytime a problem occurs this function gets called with the problem. Useful for logging etc.
   * 
   * @return Usually the a response that can be further processed.  
   */
  def createResponseWorkflow[TResult](nice: Boolean)(launderProblem: Problem => (Problem, HttpError))(onSuccess: RiftHttpResponse => TResult)(onFailure: (HttpError, RiftHttpResponse) => TResult)(reportProblem: Problem => Unit)(implicit riftWarp: RiftWarp): HttpResponseWorkflow[TResult] = {
    def handleObjectSerializationFailure(originalProblem: Problem, channel: RiftChannel with RiftHttpChannel): TResult = {
      reportProblem(originalProblem)
      val (launderedProblem, statusCode) = launderProblem(originalProblem)
      createHttpResponse(channel, nice, None)(launderedProblem)(riftWarp).fold(
        prob => {
          reportProblem(prob)
          onFailure(statusCode, RiftHttpStringResponse(DimensionString(launderedProblem.toString), "text/plain"))
        },
        rsp => onFailure(statusCode, rsp))
    }
    (channel: RiftChannel with RiftHttpChannel) =>
      (what: AnyRef) => {
        createHttpResponse(channel, nice, None)(what)(riftWarp).fold(
          prob => handleObjectSerializationFailure(prob, channel),
          onSuccess)
      }
  }

  
  def extractChannelAndTypeDescriptor(contentType: String): AlmValidation[(String, Option[TypeDescriptor])] =
    sys.error("")
    
  def transformIncomingContent[TDimension <: RiftDimension, TResult <: AnyRef](contentType: String, data: TDimension)(implicit mD: Manifest[TDimension], mTarget: Manifest[TResult], riftWarp: RiftWarp): AlmValidation[TResult] =
    for {
      (httpContentType, tdOption) <- extractChannelAndTypeDescriptor(contentType)
      channel <- riftWarp.channels.lookUpFromHttpContentType(httpContentType)
      dematerialize <- RiftWarpFuns.getRecomposeFun[TDimension, TResult](channel, None)(riftWarp.barracks.lookUpFromRematerializer[TResult])(NoFetchBlobFetch)
      dematerialized <- dematerialize(data)
    } yield dematerialized

  def transformIncomingContent[TResult <: AnyRef](getData: RiftChannel => AlmValidation[RiftDimension])(contentType: String)(implicit mTarget: Manifest[TResult], riftWarp: RiftWarp): AlmValidation[TResult] =
    for {
      (httpContentType, tdOption) <- extractChannelAndTypeDescriptor(contentType)
      channel <- riftWarp.channels.lookUpFromHttpContentType(httpContentType)
      data <- getData(channel)
      dematerialize <- RiftWarpFuns.getRecomposeFun[TResult](channel, data.getClass(), None)(riftWarp.barracks.lookUpFromRematerializer[TResult])(NoFetchBlobFetch)
      dematerialized <- dematerialize(data)
    } yield dematerialized
}

