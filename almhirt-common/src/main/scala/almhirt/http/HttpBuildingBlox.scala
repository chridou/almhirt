package almhirt.http

import almhirt.common._

object HttpBuildingBlox {
  def extractRequest[TFrom](from: TFrom)(implicit context: HttpRequestInstances[TFrom], instances: HttpInstances): AlmValidation[HttpRequest] =
    for {
      contentType <- context.contentTypeExtractor(from)
      channel <- instances.channelExtractor(contentType)
      contentTypeClassifier <- instances.classifiesChannels(channel)
      payload <- context.payloadExtractor(from, contentTypeClassifier)
    } yield HttpRequest(HttpContent(contentType, payload), Nil)

  def unmarshal[TReq](from: HttpRequest)(implicit unmarshaller: HttpUnmarshaller[TReq]): AlmValidation[TReq] =
    unmarshaller(from.content)

  def marshal[TRes](that: TRes, channel: String)(implicit marshaller: HttpMarshaller[TRes]): AlmValidation[HttpContent] =
    marshaller(that, channel)

  def processError(result: AlmValidation[HttpResponse], channel: String)(implicit instances: HttpInstances): HttpResponse =
    result.fold(
      fail => instances.errorResponseGenerator(fail, channel),
      succ => succ)
    
  def respond[T](response: HttpResponse, responder: T)(implicit consumer: HttpResponseConsumer[T]) {
    consumer(responder, response)
  }
    
  def specialResponse[T](response: HttpResponse)(implicit respGen: SpecialResponseGenerator[T]): AlmValidation[T] =
    respGen(response)
    
}