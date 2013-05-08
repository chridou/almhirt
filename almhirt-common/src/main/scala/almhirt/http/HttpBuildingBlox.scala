package almhirt.http

import scalaz.syntax.validation._
import almhirt.common._

object HttpBuildingBlox {
  def extractRequest[TFrom](from: TFrom)(implicit extractor: HttpRequestExtractor[TFrom], instances: HttpInstances): AlmValidation[HttpRequest] =
    extractor(from)(instances.channelExtractor, instances.classifiesChannels)

  def unmarshal[TReq](from: HttpRequest)(implicit unmarshaller: HttpUnmarshaller[TReq]): AlmValidation[TReq] =
    from.content match {
      case HttpNoContent => BadDataProblem("""I need content to unmarshal! You supplied "HttpNoContent".""").failure
      case c: HttpContent => unmarshaller(c)
    }

  def marshal[TRes](that: TRes, channel: String)(implicit marshaller: HttpMarshaller[TRes]): AlmValidation[HttpContent] =
    marshaller(that, channel)

  def processError(result: AlmValidation[HttpResponse], channel: String)(implicit instances: HttpInstances, problemConsumer: Consumer[Problem]): HttpResponse =
    result.fold(
      fail => instances.errorResponseGenerator(fail, channel),
      succ => succ)

  def respond[T](response: HttpResponse, responder: T)(implicit consumer: HttpResponseConsumer[T]) {
    consumer(responder, response)
  }

  def specialResponse[T](response: HttpResponse)(implicit respGen: SpecialResponseGenerator[T]): AlmValidation[T] =
    respGen(response)

}