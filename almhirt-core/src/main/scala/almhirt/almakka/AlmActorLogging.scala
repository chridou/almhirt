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
package almhirt.almakka

import akka.event._
import almhirt.common._
import almhirt.core._
import almhirt.almvalidation.kit._

/** Enables an [[akka.actor.Actor]] to log directly on [[almhirt.validation.Problem]]s 
 * 
 * Log by calling the implicit on a [[almhirt.validation.AlmValidation]]
 */
trait AlmActorLogging extends CanLogProblems { self: akka.actor.Actor =>
  val log = Logging(context.system, this)
  
  def logProblem(prob: Problem, minSeverity: Severity) {
    if(prob.severity >= minSeverity)
	  prob.severity match {
	    case NoProblem =>
	      log.debug(prob.toString)
	    case Minor =>
	      log.warning(prob.toString)
	    case Major =>
	      log.error(prob.toString)
	    case Critical =>
	      log.error(prob.toString)
	    }
  }
  
  
  implicit def almFuture2AlmValidationLoggingW[T](future: AlmFuture[T]): AlmFutureLoggingW[T]  = new AlmFutureLoggingW[T](future)
  /** Implicits to be used on a [[almhirt.concurrent.AlmFuture]] */
  final class AlmFutureLoggingW[T](future: AlmFuture[T]) {
    /** Log a [[almhirt.validation.Problem]] based on its [[almhirt.validation.Severity]] in case of a future Failure
     * 
     * @param minSeverity The minimum [[almhirt.validation.Severity]] the [[almhirt.validation.Problem]] contained in a Failure must have to be logged
     */
    def logFailure(minSeverity: Severity): AlmFuture[T] = {
       future.onFailure(logProblem(_, minSeverity))
    }
    /** Log a [[almhirt.validation.Problem]] contained in case of a Failure */
    def logFailure(): AlmFuture[T] = logFailure(NoProblem)
  }
  
}

trait AlmSystemLogging { 
//  val log = Logging(AlmAkka.actorSystem, this)
}