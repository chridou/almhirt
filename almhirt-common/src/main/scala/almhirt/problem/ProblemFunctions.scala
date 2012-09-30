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
package almhirt.problem

import almhirt.ApplicationProblem
import almhirt.Major
import almhirt.ProblemCause
import almhirt.Severity
import almhirt.SingleBadDataProblem
import almhirt.SystemProblem
import almhirt.UnspecifiedProblem

trait ProblemFunctions {
  def badData(key: String, message: String) =
    SingleBadDataProblem(message, key)
  def systemProblem(message: String, severity: Severity = Major, cause: Option[ProblemCause] = None, args: Map[String, Any] = Map()) =
    UnspecifiedProblem(message, severity, SystemProblem, args, cause)
  def applicationProblem(message: String, severity: Severity = Major, cause: Option[ProblemCause] = None, args: Map[String, Any] = Map()) =
    UnspecifiedProblem(message, severity, ApplicationProblem, args, cause)
}