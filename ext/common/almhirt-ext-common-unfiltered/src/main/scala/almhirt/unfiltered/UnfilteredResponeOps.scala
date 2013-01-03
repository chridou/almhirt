/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
