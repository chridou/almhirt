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
package almhirt.common

import scala.language.implicitConversions
import almhirt.syntax.problem._

trait CanLogProblems {
  def logProblemAsDebug(prob: Problem)
  def logProblemAsInfo(prob: Problem)
  def logProblemAsWarning(prob: Problem)
  def logProblemAsError(prob: Problem)

  implicit def problem2ProblemLoggerW(prob: Problem) = new ProblemLoggerW(prob)
  /** Implicits to be used on a problem */
  final class ProblemLoggerW(prob: Problem) {
    /** Log a [[almhirt.validation.Problem]] */
    def logAsDebug(){ logProblemAsDebug(prob) }
    def logAsInfo(){ logProblemAsInfo(prob) }
    def logAsWarning(){ logProblemAsWarning(prob) }
    def logAsError(){ logProblemAsError(prob) }
  }
}