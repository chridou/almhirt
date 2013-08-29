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

import scala.concurrent.ExecutionContext
import akka.event._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.common.AlmFuture

/**
 * Enables an [[akka.actor.Actor]] to log directly on [[almhirt.validation.Problem]]s
 *
 * Log by calling the implicit on a [[almhirt.validation.AlmValidation]]
 */
trait AlmActorLogging extends CanLogProblems { self: akka.actor.Actor =>
  val log = Logging(context.system, this)

  override def logProblemAsDebug(prob: Problem) = log.debug(prob.withArg("actor", self.path).toString)
  override def logProblemAsInfo(prob: Problem) = log.info(prob.withArg("actor", self.path).toString)
  override def logProblemAsWarning(prob: Problem) = log.warning(prob.withArg("actor", self.path).toString)
  override def logProblemAsError(prob: Problem) = log.error(prob.withArg("actor", self.path).toString)
}
