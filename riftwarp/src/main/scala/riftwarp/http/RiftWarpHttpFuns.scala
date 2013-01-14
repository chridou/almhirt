package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.http._
import riftwarp._
import riftwarp.RiftStringBasedDimension


object RiftWarpHttpFuns {
  def extractContentType(rawContent: String): AlmValidation[HttpContentType] =
    HttpContentType.parse(rawContent)

  def transformIncomingContent[TResult <: AnyRef](riftWarp: RiftWarp)(contentType: HttpContentType, content: RiftHttpDimension)(implicit mResult: Manifest[TResult]): AlmValidation[TResult] = {
    val td = option.cata(contentType.tryGetTypeDescriptor)(td => td, TypeDescriptor(mResult.runtimeClass))
    for {
      dematerialize <- RiftWarpFuns.getRecomposeFun[TResult](contentType.channel, content.getClass().asInstanceOf[Class[_ <: RiftDimension]], None)(remat => riftWarp.barracks.lookUpFromRematerializer[TResult](remat, Some(td)))(NoFetchBlobFetch)(mResult, riftWarp)
      dematerialized <- dematerialize(content)
    } yield dematerialized
  }

  def withRequest[TResult](riftWarp: RiftWarp)(nice: Boolean)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(onBadRequest: (HttpError, RiftHttpData) => TResult)(getContentType: () => AlmValidation[String])(getBody: RiftHttpChannel => AlmValidation[RiftDimension with RiftHttpDimension])(compute: (HttpContentType, RiftDimension with RiftHttpDimension) => TResult): TResult =
    getContentType().flatMap(rawContent =>
      extractContentType(rawContent)).fold(
      fail => onBadRequest(Http_400_Bad_Request, RiftHttpStringData(HttpContentType.PlainText, launderProblem(fail)._1.toString())),
      contentType =>
          (for {
            body <- getBody(contentType.channel)
          } yield body).fold(
            fail => {
              val (problem, _) = launderProblem(fail)
              val resp = createHttpProblemResponse(riftWarp)(reportProblem)(contentType.channel, nice, None)(problem)
              onBadRequest(Http_400_Bad_Request, resp)
            },
            body => compute(contentType, body)))

  def createHttpData(riftWarp: RiftWarp)(channel: RiftHttpChannel, nice: Boolean, contentExtOverride: Option[String] = None)(what: AnyRef): AlmValidation[RiftHttpData] = {
    for {
      decomposer <- riftWarp.barracks.getDecomposerForAny(what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[AnyRef](channel, channel.httpDimensionType(nice), None)(NoDivertBlobDivert)(riftWarp)
      dematerialized <- dematerialzeFun(what, decomposer)
      contentType <- HttpContentType(decomposer.typeDescriptor, channel, Map.empty[String, String]).success
      response <- dematerialized match {
        case dim: RiftStringBasedDimension => RiftHttpStringData(contentType, dim.manifestation).success
        case dim: RiftByteArrayBasedDimension => RiftHttpBinaryData(contentType, dim.manifestation).success
        case x => UnspecifiedProblem("Not a valid HTTP-Dimension: %s".format(x.getClass().getName())).failure
      }
    } yield response
  }

  def createHttpProblemResponse(riftWarp: RiftWarp)(reportProblem: Problem => Unit)(channel: RiftHttpChannel, nice: Boolean, contentExtOverride: Option[String] = None)(what: Problem): RiftHttpData = {
    (for {
      decomposer <- riftWarp.barracks.getDecomposerForAny[Problem](what)
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun[Problem](channel, channel.httpDimensionType(nice), None)(NoDivertBlobDivert)(riftWarp)
      dematerialized <- dematerialzeFun(what, decomposer)
      contentType <- HttpContentType( decomposer.typeDescriptor, channel, Map.empty[String, String]).success
      response <- dematerialized match {
        case dim: RiftStringBasedDimension => RiftHttpStringData(contentType, dim.manifestation).success
        case dim: RiftByteArrayBasedDimension => RiftHttpBinaryData(contentType, dim.manifestation).success
        case x => UnspecifiedProblem("Not a valid HTTP-Dimension: %s".format(x.getClass().getName())).failure
      }
    } yield response).fold(
      prob => {
        reportProblem(prob)
        RiftHttpStringData(HttpContentType.PlainText, what.toString())
      },
      succ => succ)
  }

  def createProblemHandler[T](riftWarp: RiftWarp)(nice: Boolean)(reportProblem: Problem => Unit): (RiftHttpChannel) => (HttpError, Problem, (HttpError, RiftHttpData) => T) => T =
    (channel: RiftHttpChannel) =>
      (errorCode: HttpError, problem: Problem, responder: (HttpError, RiftHttpData) => T) =>
        createHttpData(riftWarp)(channel, nice, None)(problem).fold(
          fail => {
            reportProblem(fail)
            responder(errorCode, RiftHttpStringData(HttpContentType.PlainText, problem.toString))
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
  def createResponseWorkflow[TResult](riftWarp: RiftWarp)(launderProblem: Problem => (Problem, HttpError))(reportProblem: Problem => Unit)(nice: Boolean)(onSuccess: (HttpSuccess, RiftHttpData) => TResult)(onFailure: (HttpError, RiftHttpData) => TResult): HttpResponseWorkflow[TResult] = {
    (channel: RiftHttpChannel) =>
      (what: AnyRef, successCode: HttpSuccess) => {
        createHttpData(riftWarp)(channel, nice, None)(what).fold(
          prob => {
            val (problem, code) = launderProblem(prob)
            val resp = createHttpProblemResponse(riftWarp)(reportProblem)(channel, nice, None)(prob)
            onFailure(code, resp)
          },
          succ => onSuccess(successCode, succ))
      }
  }
}

