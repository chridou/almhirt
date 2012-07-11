package almhirt.unfiltered

import scalaz.{Success, Failure}
import org.jboss.netty.handler.codec.http.HttpResponse
import akka.dispatch.Future
import unfiltered.response.ResponseFunction
import almhirt.validation.AlmValidation
import almhirt.concurrent.AlmFuture

trait ProblemToUnfilteredResponseImplicits {
  implicit def hdrFuture2AlmFutureW[T](hdrFuture: AlmFuture[T]) = new almhirtFutureW[T](hdrFuture)
  implicit def akkaFuture2AlmAkkaFutureW[T](akkaFuture: Future[AlmValidation[T]]) = new AkkaFutureW[T](akkaFuture)

  final class almhirtFutureW[T](almFuture: AlmFuture[T]) {
    def respond(responder: unfiltered.Async.Responder[HttpResponse], createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): Future[AlmValidation[T]] = {
      almFuture.onComplete({
        case Success(r) => responder.respond(createSuccessResponse(r))
        case Failure(problem) => responder.respond(ProblemToUnfilteredResponse.problemToResponse(problem))
      })
    }
  }
  final class AkkaFutureW[T](akkaFuture: Future[AlmValidation[T]]) {
    def respond(responder: unfiltered.Async.Responder[HttpResponse], createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): Future[AlmValidation[T]] = {
      new AlmFuture[T](akkaFuture).respond(responder, createSuccessResponse)		
    }
  }
}

object ProblemToUnfilteredResponseImplicits extends ProblemToUnfilteredResponseImplicits {
}