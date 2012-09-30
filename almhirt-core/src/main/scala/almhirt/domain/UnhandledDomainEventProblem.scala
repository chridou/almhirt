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
package almhirt.domain

import almhirt._

/** Used to indicate that an event couldn't be handled by an aggregate root's handler. 
 * This is a major problem as it indicates a software defect.
 */
case class UnhandledDomainEventProblem(message: String, event: DomainEvent, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = UnhandledDomainEventProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}
