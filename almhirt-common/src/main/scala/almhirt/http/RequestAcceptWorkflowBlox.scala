package almhirt.http

import almhirt.common._

object RequestAcceptWorkflowBlox {
  import HttpBuildingBlox._
  def toResponse[TFrom, T](f: T => Unit, acceptCode: HttpStatusCode)(implicit extractor: HttpRequestExtractor[TFrom], instances: HttpInstances, unmarshaller: HttpUnmarshaller[T], problemConsumer: Consumer[Problem]): TFrom => HttpResponse =
    (source: TFrom) =>
      extractRequest(source).flatMap(req =>
        instances.channelExtractor(req.preferredContentType).map((req, _))).fold(
        fail => instances.errorResponseGenerator(fail, "text/plain"),
        reqAndReplyChannel =>
          unmarshal(reqAndReplyChannel._1).fold(
             fail => instances.errorResponseGenerator(fail, reqAndReplyChannel._2),
             succ => {
               f(succ)
               HttpResponse(acceptCode, HttpNoContent)
             }))
}