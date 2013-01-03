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

sealed trait ProblemCategory{
  def and(other: ProblemCategory): ProblemCategory = 
    (this,other) match {
    case(SystemProblem,_) => SystemProblem
    case(_,SystemProblem) => SystemProblem
    case _  => ApplicationProblem
  }
}

case object SystemProblem extends ProblemCategory
case object ApplicationProblem extends ProblemCategory

object ProblemCategory {
  def fromString(str: String): AlmValidation[ProblemCategory] =
    str.toLowerCase() match {
      case "systemproblem" => scalaz.Success(SystemProblem)
      case "applicationproblem" => scalaz.Success(ApplicationProblem)
      case x => scalaz.Failure(BadDataProblem("'%s' is not a problem category".format(x)))
    }
}
