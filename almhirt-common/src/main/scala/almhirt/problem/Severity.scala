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

/* Severity of a problem. 
 * 
 * NoProblem < Minor < Major < Fatal
 */
sealed trait Severity extends Ordered[Severity] {
  def and(other: Severity): Severity =
    (this, other) match {
      case (Critical, _) ⇒ Critical
      case (_, Critical) ⇒ Critical
      case (Major, _) ⇒ Major
      case (_, Major) ⇒ Major
      case _ ⇒ Minor
    }
  /** Used for comparison */
  def level: Int
  def compare(that: Severity) = this.level compare (that.level)
  def parseableString: String
}
final case object Critical extends Severity {
  val level = 3
  val parseableString = "critical"
}
final case object Major extends Severity {
  val level = 2
  val parseableString = "major"
}
final case object Minor extends Severity {
  val level = 1
  val parseableString = "minor"
}

object Severity {
  def fromString(str: String): almhirt.common.AlmValidation[Severity] =
    str.toLowerCase() match {
      case "minor" ⇒ scalaz.Success(Minor)
      case "major" ⇒ scalaz.Success(Major)
      case "critical" ⇒ scalaz.Success(Critical)
      case x ⇒ scalaz.Failure(problemtypes.ParsingProblem(""""$str" is not a severity"""))
    }
}

