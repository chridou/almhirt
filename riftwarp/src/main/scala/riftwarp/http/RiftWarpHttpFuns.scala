package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.http._
import riftwarp._
import riftwarp.RiftStringBasedDimension

object RiftWarpHttpFuns {
  case class RiftHttpFunsSettings(
    riftWarp: RiftWarp,
    nice: Boolean,
    launderProblem: Problem => (Problem, HttpError),
    reportProblem: Problem => Unit,
    defaultChannel: RiftHttpChannel,
    contentTypePrefix: Option[String])

  def createHttpDataFromRequest(getContentType: () => Option[String], getBody: RiftHttpBodyType => AlmValidation[RiftHttpBody], contentTypePrefix: Option[String]): AlmValidation[RiftHttpData] =
    for {
      contentType <- option.cata(getContentType())(ct => RiftHttpContentType.parse(ct, contentTypePrefix), RiftHttpNoContentContentType.success)
      data <- contentType match {
        case RiftHttpNoContentContentType =>
          RiftHttpNoContentData.success
        case withChannel: RiftHttpContentTypeWithChannel =>
          getBody(withChannel.channel.httpBodyType).flatMap(_.toHttpData(contentType))
      }
    } yield data

  def transformHttpData[To <: AnyRef](riftWarp: RiftWarp)(content: RiftHttpDataWithContent)(implicit mResult: Manifest[To]): AlmValidation[To] = {
    val contentType = content.contentType
    val td = option.cata(contentType.tryGetRiftDescriptor)(td => td, RiftDescriptor(mResult.runtimeClass))
    for {
      data <- content.toRiftDimension
      recompose <- RiftWarpFuns.getRecomposeFun[To](contentType.channel, data.getClass().asInstanceOf[Class[_ <: RiftDimension]], None)(remat => riftWarp.barracks.lookUpFromRematerializer[To](remat, Some(td)))(NoFetchBlobFetch)(mResult, riftWarp)
      recomposed <- recompose(data)
    } yield recomposed
  }

  def createHttpProblemResponseData(settings: RiftHttpFunsSettings)(what: Problem, optReqChannel: Option[RiftHttpChannel]): RiftHttpDataWithContent = {
    (for {
      decomposer <- settings.riftWarp.barracks.getDecomposerFor[Problem](what)
      channel <- option.cata(optReqChannel)(identity, settings.defaultChannel).success
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun(channel, channel.httpDimensionType(settings.nice), None)(NoDivertBlobDivert)(settings.riftWarp)
      dematerialized <- dematerialzeFun(what, decomposer)
      contentType <- RiftHttpContentType(decomposer.riftDescriptor, channel, Map.empty[String, String]).success
      response <- dematerialized.toHttpData(contentType)
    } yield response).fold(
      prob => {
        settings.reportProblem(prob)
        RiftHttpDataWithContent(RiftHttpContentType.PlainText, RiftStringBody(what.toString()))
      },
      identity)
  }

  def createHttpData[TResp <: AnyRef](settings: RiftHttpFunsSettings)(what: TResp, optReqChannel: Option[RiftHttpChannel]): AlmValidation[RiftHttpDataWithContent] =
    for {
      decomposer <- settings.riftWarp.barracks.getDecomposerFor[TResp](what)
      channel <- option.cata(optReqChannel)(identity, settings.defaultChannel).success
      dematerialzeFun <- RiftWarpFuns.getDematerializationFun(channel, channel.httpDimensionType(settings.nice), None)(NoDivertBlobDivert)(settings.riftWarp)
      dematerialized <- dematerialzeFun(what, decomposer)
      contentType <- RiftHttpContentType(decomposer.riftDescriptor, channel, Map.empty[String, String]).success
      response <- dematerialized.toHttpData(contentType)
    } yield response

  def withRequestData[TEntity <: AnyRef](settings: RiftHttpFunsSettings, httpData: RiftHttpDataWithContent, computeResponse: (TEntity) => RiftHttpResponse)(implicit mEntity: Manifest[TEntity]): RiftHttpResponse =
    transformHttpData[TEntity](settings.riftWarp)(httpData).fold(
      fail => {
        settings.reportProblem(fail)
        val (prob, code) = settings.launderProblem(fail)
        RiftHttpResponse(code, createHttpProblemResponseData(settings)(prob, httpData.contentType.tryGetChannel))
      },
      succ => computeResponse(succ))

  def withRequest[TEntity <: AnyRef](settings: RiftHttpFunsSettings, getHttpData: () => AlmValidation[RiftHttpData], computeResponse: (TEntity) => RiftHttpResponse)(implicit mEntity: Manifest[TEntity]): RiftHttpResponse =
    getHttpData().fold(
      fail => {
        val (prob, code) = settings.launderProblem(fail)
        RiftHttpResponse(Http_400_Bad_Request, createHttpProblemResponseData(settings)(prob, None))
      },
      succ =>
        succ match {
          case RiftHttpNoContentData =>
            RiftHttpResponse(Http_400_Bad_Request, RiftHttpNoContentData)
          case RiftHttpDataWithContent(contentType, body) =>
            withRequestData(settings, RiftHttpDataWithContent(contentType, body), computeResponse)
        })

  def withRequestDataOnFuture[TEntity <: AnyRef](settings: RiftHttpFunsSettings, httpData: RiftHttpDataWithContent, computeResponse: (TEntity) => AlmFuture[RiftHttpResponse])(implicit mEntity: Manifest[TEntity]): AlmFuture[RiftHttpResponse] =
    transformHttpData[TEntity](settings.riftWarp)(httpData).fold(
      fail => {
        settings.reportProblem(fail)
        val (prob, code) = settings.launderProblem(fail)
        AlmFuture.successful(RiftHttpResponse(code, createHttpProblemResponseData(settings)(prob, httpData.contentType.tryGetChannel)))
      },
      succ => computeResponse(succ))

  def withRequestOnFuture[TEntity <: AnyRef](settings: RiftHttpFunsSettings, getHttpData: () => AlmValidation[RiftHttpData], computeResponse: (TEntity) => AlmFuture[RiftHttpResponse])(implicit mEntity: Manifest[TEntity]): AlmFuture[RiftHttpResponse] =
    getHttpData().fold(
      fail => {
        val (prob, code) = settings.launderProblem(fail)
        AlmFuture.successful(RiftHttpResponse(Http_400_Bad_Request, createHttpProblemResponseData(settings)(prob, None)))
      },
      succ =>
        succ match {
          case RiftHttpNoContentData =>
            AlmFuture.successful(RiftHttpResponse(Http_400_Bad_Request, RiftHttpNoContentData))
          case RiftHttpDataWithContent(contentType, body) =>
            withRequestDataOnFuture(settings, RiftHttpDataWithContent(contentType, body), computeResponse)
        })

  def respond[TResp <: AnyRef](settings: RiftHttpFunsSettings)(okStatus: HttpSuccess, channel: RiftHttpChannel)(computeResponseValue: () => AlmValidation[Option[TResp]]): RiftHttpResponse =
    computeResponseValue().fold(
      fail => {
        settings.reportProblem(fail)
        val (prob, code) = settings.launderProblem(fail)
        RiftHttpResponse(code, createHttpProblemResponseData(settings)(prob, Some(channel)))
      },
      resultOpt =>
        option.cata(resultOpt)(
          result =>
            createHttpData[TResp](settings)(result, Some(channel)).fold(
              fail => {
                settings.reportProblem(fail)
                val (prob, code) = settings.launderProblem(fail)
                RiftHttpResponse(code, createHttpProblemResponseData(settings)(prob, Some(channel)))
              },
              succ => RiftHttpResponse(okStatus, succ)),
          RiftHttpResponse(okStatus, RiftHttpNoContentData)))

  def respondOnFuture[TResp <: AnyRef](settings: RiftHttpFunsSettings, okStatus: HttpSuccess, channel: RiftHttpChannel, computeResponseValue: () => AlmFuture[Option[TResp]])(implicit hasExecutionContext: HasExecutionContext): AlmFuture[RiftHttpResponse] =
    computeResponseValue().fold(
      fail => {
        settings.reportProblem(fail)
        val (prob, code) = settings.launderProblem(fail)
        RiftHttpResponse(code, createHttpProblemResponseData(settings)(prob, Some(channel)))
      },
      resultOpt =>
        option.cata(resultOpt)(
          result =>
            createHttpData[TResp](settings)(result, Some(channel)).fold(
              fail => {
                settings.reportProblem(fail)
                val (prob, code) = settings.launderProblem(fail)
                RiftHttpResponse(code, createHttpProblemResponseData(settings)(prob, Some(channel)))
              },
              succ => RiftHttpResponse(okStatus, succ)),
          RiftHttpResponse(okStatus, RiftHttpNoContentData)))

  def processRequest[TReq <: AnyRef, TResp <: AnyRef](settings: RiftHttpFunsSettings, getHttpData: () => AlmValidation[RiftHttpData], okStatus: HttpSuccess, computeResponse: TReq => AlmValidation[Option[TResp]])(implicit mReq: Manifest[TReq]): RiftHttpResponse =
    getHttpData().fold(
      fail => {
        val (prob, code) = settings.launderProblem(fail)
        RiftHttpResponse(Http_400_Bad_Request, createHttpProblemResponseData(settings)(prob, None))
      },
      httpData => withRequest[TReq](settings, () => httpData.success, req => {
        val channel = httpData.contentType.tryGetChannel.getOrElse(settings.defaultChannel)
        respond[TResp](settings)(okStatus, channel)(() => computeResponse(req))
      }))

  def processRequestRespondOnFuture[TReq <: AnyRef, TResp <: AnyRef](settings: RiftHttpFunsSettings, getHttpData: () => AlmValidation[RiftHttpData], okStatus: HttpSuccess, computeResponse: TReq => AlmFuture[Option[TResp]])(implicit mReq: Manifest[TReq], hasExecutionContext: HasExecutionContext): AlmFuture[RiftHttpResponse] =
    getHttpData().fold(
      fail => {
        val (prob, code) = settings.launderProblem(fail)
        AlmFuture.successful(RiftHttpResponse(Http_400_Bad_Request, createHttpProblemResponseData(settings)(prob, None)))
      },
      httpData => withRequestOnFuture[TReq](settings, () => httpData.success, req => {
        val channel = httpData.contentType.tryGetChannel.getOrElse(settings.defaultChannel)
        respondOnFuture[TResp](settings, okStatus, channel, () => computeResponse(req))
      }))

  def futureResponder(settings: RiftHttpFunsSettings, responder: RiftHttpResponse => Unit, futureRes: AlmFuture[RiftHttpResponse])(implicit hasExecutionContext: HasExecutionContext) {
    futureRes.onComplete(
      fail => {
        val (prob, code) = settings.launderProblem(fail)
        responder(RiftHttpResponse(Http_400_Bad_Request, createHttpProblemResponseData(settings)(prob, None)))
      },
      succ => responder(succ))
  }

  def transformResponse[T <: AnyRef](settings: RiftHttpFunsSettings)(response: RiftHttpResponse): AlmValidation[T] = {
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

