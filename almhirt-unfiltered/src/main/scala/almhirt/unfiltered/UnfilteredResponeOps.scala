package almhirt.unfiltered

package syntax

import scalaz.syntax.Ops
import org.jboss.netty.handler.codec.http.HttpResponse
import akka.dispatch.Future
import unfiltered.response.ResponseFunction
import almhirt.validation._
import almhirt.concurrent.AlmFuture


trait UnfilteredResponeOps0[T] extends Ops[AlmFuture[T]] {
  def respond(responder: unfiltered.Async.Responder[HttpResponse], createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): AlmFuture[T] = 
    self.onComplete(
        prob => responder.respond(UnfilteredResponseFunctions.problemToResponse(prob)),
        r => responder.respond(createSuccessResponse(r)))
}

trait UnfilteredResponeOps1[T] extends Ops[Future[AlmValidation[T]]] {
  import UnfilteredResponeOps._
  def respond(
      responder: unfiltered.Async.Responder[HttpResponse], 
      createSuccessResponse: Function[T,ResponseFunction[HttpResponse]]): Future[AlmValidation[T]] = {
    self.respond(responder, createSuccessResponse)		
  }
}
  
trait UnfilteredResponeOps2 extends Ops[Problem]{
  def toResponseFunction(): ResponseFunction[HttpResponse] = 
    UnfilteredResponseFunctions.problemToResponse(self)
}

trait ToUnfilteredResponeOps {
  implicit def FromHdrFutureToUnfilteredResponeOps0[T](a: AlmFuture[T]): UnfilteredResponeOps0[T] = new UnfilteredResponeOps0[T]{ def self = a }
  implicit def FromAkkaFutureValidationToUnfilteredResponeOps1[T](a: Future[AlmValidation[T]]): UnfilteredResponeOps1[T] = new UnfilteredResponeOps1[T]{ def self = a }
  implicit def FromProblemToUnfilteredResponeOps2(a: Problem): UnfilteredResponeOps2 = new UnfilteredResponeOps2{ def self = a }
}

object UnfilteredResponeOps extends ToUnfilteredResponeOps