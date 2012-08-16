package almhirt.unfiltered

import scalaz.{Success, Failure}
import org.jboss.netty.handler.codec.http.HttpResponse
import akka.dispatch.Future
import unfiltered.response.ResponseFunction
import almhirt.validation.AlmValidation
import almhirt.validation.Problem
import almhirt.concurrent.AlmFuture

trait ProblemToUnfilteredResponseImplicits {
  implicit def hdrFuture2AlmFutureW[T](hdrFuture: AlmFuture[T]) = new AlmhirtFutureW[T](hdrFuture)
  implicit def akkaFuture2AlmAkkaFutureW[T](akkaFuture: Future[AlmValidation[T]]) = new AkkaFutureW[T](akkaFuture)

  final class AlmhirtFutureW[T](almFuture: AlmFuture[T]) {
    def respond(responder: unfiltered.Async.Responder[HttpResponse], createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): AlmFuture[T] = 
      almFuture.onComplete(
          prob => responder.respond(ProblemToUnfilteredResponse.problemToResponse(prob)),
          r => responder.respond(createSuccessResponse(r)))
  }
  final class AkkaFutureW[T](akkaFuture: Future[AlmValidation[T]]) {
    def respond(
        responder: unfiltered.Async.Responder[HttpResponse], 
        createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): Future[AlmValidation[T]] = {
      akkaFuture.respond(responder, createSuccessResponse)		
    }
  }
  
  implicit def problem2ProblemW(prob: Problem): ProblemW = new ProblemW(prob)
  final class ProblemW(prob: Problem){
    def toResponseFunction(): ResponseFunction[HttpResponse] = 
      ProblemToUnfilteredResponse.problemToResponse(prob)
  }
}

object ProblemToUnfilteredResponseImplicits extends ProblemToUnfilteredResponseImplicits {
}