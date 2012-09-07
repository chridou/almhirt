package almhirt.ext.unfiltered

import scalaz.syntax.Ops
import org.jboss.netty.handler.codec.http.HttpResponse
import akka.dispatch.Future
import unfiltered.response.ResponseFunction
import unfiltered.Async
import almhirt._


trait UnfilteredResponeOps0[T] extends Ops[AlmFuture[T]] with UnfilteredResponseFunctions {
  def respond(responder: Async.Responder[HttpResponse], createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): AlmFuture[T] = 
    self.onComplete(
        prob => responder.respond(problemToResponse(prob)),
        r => responder.respond(createSuccessResponse(r)))
}

trait UnfilteredResponeOps1[T] extends Ops[Future[AlmValidation[T]]] with UnfilteredResponseFunctions with ToUnfilteredResponeOps {
  def respond(
      responder: Async.Responder[HttpResponse], 
      createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): Future[AlmValidation[T]] = {
    self.respond(responder, createSuccessResponse)		
  }
}
  
trait UnfilteredResponeOps2 extends Ops[Problem] with UnfilteredResponseFunctions{
  def toResponseFunction(): ResponseFunction[HttpResponse] = 
    problemToResponse(self)
}

trait ToUnfilteredResponeOps {
  implicit def FromHdrFutureToUnfilteredResponeOps0[T](a: AlmFuture[T]): UnfilteredResponeOps0[T] = new UnfilteredResponeOps0[T]{ def self = a }
  implicit def FromAkkaFutureValidationToUnfilteredResponeOps1[T](a: Future[AlmValidation[T]]): UnfilteredResponeOps1[T] = new UnfilteredResponeOps1[T]{ def self = a }
  implicit def FromProblemToUnfilteredResponeOps2(a: Problem): UnfilteredResponeOps2 = new UnfilteredResponeOps2{ def self = a }
}
