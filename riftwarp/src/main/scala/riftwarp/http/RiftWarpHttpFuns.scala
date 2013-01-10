package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.http._
import riftwarp._
import riftwarp.RiftStringBasedDimension

sealed trait HttpRequestDataType
object BinaryDataRequest extends HttpRequestDataType
object StringDataRequest extends HttpRequestDataType

object RiftWarpHttpFuns {
  def getRequiredRequestDataType(channel: RiftChannel with RiftHttpChannel): AlmValidation[HttpRequestDataType] = {
    channel match {
      case ch: RiftText => NotSupportedProblem("Channel RiftText").failure
      case ch: RiftMap => NotSupportedProblem("Channel RiftMap").failure
      case ch: RiftJson => StringDataRequest.success
      case ch: RiftBson => NotSupportedProblem("Channel RiftBson").failure
      case ch: RiftXml => StringDataRequest.success
      case ch: RiftMessagePack => NotSupportedProblem("Channel RiftMessagePack").failure
      case ch: RiftProtocolBuffers => NotSupportedProblem("Channel RiftProtocolBuffers").failure
      case x => NotSupportedProblem("Channel '%s'".format(x.getClass())).failure
    }
  }

  def extractChannelAndTypeDescriptor(contentType: String): AlmValidation[(RiftChannel with RiftHttpChannel, Option[TypeDescriptor])] =
    sys.error("")

  def transformIncomingContent[TResult <: AnyRef](riftWarp: RiftWarp)(channel: RiftChannel with RiftHttpChannel, typeDescriptor: Option[TypeDescriptor], content: RiftHttpDimension)(implicit mResult: Manifest[TResult]): AlmValidation[TResult] = {
    val td = option.cata(typeDescriptor)(td => td, TypeDescriptor(mResult.runtimeClass))
    for {
      dematerialize <- RiftWarpFuns.getRecomposeFun[TResult](channel, content.getClass().asInstanceOf[Class[_ <: RiftDimension]], None)(remat => riftWarp.barracks.lookUpFromRematerializer[TResult](remat, Some(td)))(NoFetchBlobFetch)(mResult, riftWarp)
      dematerialized <- dematerialize(content)
    } yield dematerialized
  }

  def withRequest[TResult](riftWarp: RiftWarp)(nice: Boolean)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(onBadRequest: (HttpError, RiftHttpResponse) => TResult)(getContentType: () => AlmValidation[String])(getBody: HttpRequestDataType => AlmValidation[RiftDimension with RiftHttpDimension])(compute: (RiftChannel with RiftHttpChannel, Option[TypeDescriptor], RiftDimension with RiftHttpDimension) => TResult): TResult =
    getContentType().flatMap(contentType =>
      extractChannelAndTypeDescriptor(contentType)).fold(
      fail => onBadRequest(Http_400_Bad_Request, RiftHttpStringResponse(launderProblem(fail)._1.toString(), "text/plain")),
      {
        case (channel, typeDescriptor) =>
          (for {
            requestDataType <- getRequiredRequestDataType(channel)
            body <- getBody(requestDataType)
          } yield body).fold(
            fail => {
              val (problem, _) = launderProblem(fail)
              val resp = createHttpProblemResponse(riftWarp)(reportProblem)(channel, nice, None)(problem)
              onBadRequest(Http_400_Bad_Request, resp)
            },
            body => compute(channel, typeDescriptor, body))
      })

  private trait ContentTypeContainer { def create(td: TypeDescriptor): String }
  private case class ContentTypeExt(contentExt: String) extends ContentTypeContainer { def create(td: TypeDescriptor) = td.identifier + "+" + contentExt }
  private case class ContentType(contentType: String) extends ContentTypeContainer { def create(td: TypeDescriptor) = contentType }

  private object ContentTypeContainer {
    def apply(channel: RiftChannel with RiftHttpChannel, contentExtOverride: Option[String] = None): scalaz.Validation[RiftWarpProblem, ContentTypeContainer] =
      option.cata(contentExtOverride)(
        ext => ContentTypeExt(ext).success,
        option.cata(channel.httpContentTypeExt)(
          ext => ContentTypeExt(ext).success,
          option.cata(channel.httpContentType)(
            ct => ContentType(ct).success,
            RiftWarpProblem("Could determine a content type extension. The channel was '%s'".format(channel.channelType)).failure)))
  }

  def createHttpResponse(riftWarp: RiftWarp)(channel: RiftChannel with RiftHttpChannel, nice: Boolean, contentExtOverride: Option[String] = None)(what: AnyRef): AlmValidation[RiftHttpResponse] = {
    for {
      contentExt <- ContentTypeContainer(channel, contentExtOverride)
      decomposer <- riftWarp.barracks.getDecomposerForAny(what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[AnyRef](channel, channel.httpDimensionType(nice), None)(NoDivertBlobDivert)(riftWarp)
      dematerialized <- dematerialzeFun(what, decomposer)
      response <- dematerialized match {
        case dim: RiftStringBasedDimension => RiftHttpStringResponse(dim.manifestation, contentExt.create(decomposer.typeDescriptor)).success
        case dim: RiftByteArrayBasedDimension => RiftHttpBinaryResponse(dim.manifestation, contentExt.create(decomposer.typeDescriptor)).success
        case x => UnspecifiedProblem("Not a valid HTTP-Dimension: %s".format(x.getClass().getName())).failure
      }
    } yield response
  }

  def createHttpProblemResponse(riftWarp: RiftWarp)(reportProblem: Problem => Unit)(channel: RiftChannel with RiftHttpChannel, nice: Boolean, contentExtOverride: Option[String] = None)(what: Problem): RiftHttpResponse = {
    (for {
      contentExt <- ContentTypeContainer(channel, contentExtOverride)
      decomposer <- riftWarp.barracks.getDecomposerForAny[Problem](what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[Problem](channel, channel.httpDimensionType(nice), None)(NoDivertBlobDivert)(riftWarp)
      dematerialized <- dematerialzeFun(what, decomposer)
      response <- dematerialized match {
        case dim: RiftStringBasedDimension => RiftHttpStringResponse(dim.manifestation, contentExt.create(decomposer.typeDescriptor)).success
        case dim: RiftByteArrayBasedDimension => RiftHttpBinaryResponse(dim.manifestation, contentExt.create(decomposer.typeDescriptor)).success
        case x => UnspecifiedProblem("Not a valid HTTP-Dimension: %s".format(x.getClass().getName())).failure
      }
    } yield response).fold(
      prob => {
        reportProblem(prob)
        RiftHttpStringResponse(what.toString(), "text/plain")
      },
      succ => succ)
  }

  def createProblemHandler[T](riftWarp: RiftWarp)(nice: Boolean)(reportProblem: Problem => Unit): (RiftChannel with RiftHttpChannel) => (HttpError, Problem, (HttpError, RiftHttpResponse) => T) => T =
    (channel: RiftChannel with RiftHttpChannel) =>
      (errorCode: HttpError, problem: Problem, responder: (HttpError, RiftHttpResponse) => T) =>
        createHttpResponse(riftWarp)(channel, nice, None)(problem).fold(
          fail => {
            reportProblem(fail)
            responder(errorCode, RiftHttpStringResponse(problem.toString, "text/plain"))
          },
          succ => responder(errorCode, succ))

  /**
   * Creates a function of type [[riftwarp.HttpResponseWorkflow[T]]] that can serialize an AnyRef and create a response based on the result.
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
  def createResponseWorkflow[TResult](riftWarp: RiftWarp)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(nice: Boolean)(onSuccess: (HttpSuccess, RiftHttpResponse) => TResult)(onFailure: (HttpError, RiftHttpResponse) => TResult): HttpResponseWorkflow[TResult] = {
    (channel: RiftChannel with RiftHttpChannel) =>
      (what: AnyRef, successCode: HttpSuccess) => {
        createHttpResponse(riftWarp)(channel, nice, None)(what).fold(
          prob => {
            val (problem, code) = launderProblem(prob)
            val resp = createHttpProblemResponse(riftWarp)(reportProblem)(channel, nice, None)(prob)
            onFailure(code, resp)
          },
          succ => onSuccess(successCode, succ))
      }
  }
}

