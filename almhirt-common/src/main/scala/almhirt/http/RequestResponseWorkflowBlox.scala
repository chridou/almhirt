package almhirt.http

import almhirt.common._

object RequestResponseWorkflowBlox {
  import HttpBuildingBlox._
  def toResponse[TFrom, T, U](f: T => AlmValidation[U], successCode: HttpStatusCode)(implicit context: HttpRequestExtractor[TFrom], instances: HttpInstances, unmarshaller: HttpUnmarshaller[T], marshaller: HttpMarshaller[U], problemConsumer: Consumer[Problem]): TFrom => HttpResponse =
    (source: TFrom) =>
      extractRequest(source).flatMap(req =>
        instances.channelExtractor(req.preferredContentType).map((req, _))).fold(
        fail => instances.errorResponseGenerator(fail, "text/plain"),
        reqAndReplyChannel =>
          (for {
            unmarshalled <- unmarshal(reqAndReplyChannel._1)
            res <- f(unmarshalled)
            marshalled <- marshaller(res, reqAndReplyChannel._2)
          } yield marshalled).fold(
            fail =>
              instances.errorResponseGenerator(fail, reqAndReplyChannel._2),
            succ =>
              HttpResponse(successCode, succ)))


  def toResponseF[TFrom, T, U](f: T => AlmFuture[U], successCode: HttpStatusCode)(implicit context: HttpRequestExtractor[TFrom], instances: HttpInstances, unmarshaller: HttpUnmarshaller[T], marshaller: HttpMarshaller[U], problemConsumer: Consumer[Problem], hec: HasExecutionContext): TFrom => AlmFuture[HttpResponse] =
    (source: TFrom) => {
      val reqAndReplyChannelF = AlmFuture {
        extractRequest(source).flatMap(req =>
          instances.channelExtractor(req.preferredContentType).map((req, _)))
      }
      (for {
        reqAndReplyChannel <- reqAndReplyChannelF
        unmarshalled <- AlmFuture { unmarshal(reqAndReplyChannel._1) }
        res <- f(unmarshalled)
        marshalled <- AlmFuture { marshaller(res, reqAndReplyChannel._2) }
      } yield HttpResponse(successCode, marshalled)).foldF(
        fail => reqAndReplyChannelF.fold(
          outerFail => instances.errorResponseGenerator(outerFail, "text/plain"),
          reqAndReplyChannel => instances.errorResponseGenerator(fail, reqAndReplyChannel._2)),
        succ => AlmFuture.successful { succ })
    }

        
}