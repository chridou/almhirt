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
package almhirt

trait CanLogProblems {
  def logProblem(prob: Problem, minSeverity: Severity): Unit

  implicit def problem2ProblemLoggerW(prob: Problem) = new ProblemLoggerW(prob)
  /** Implicits to be used on a problem */
  final class ProblemLoggerW(prob: Problem) {
    /** Log a [[almhirt.validation.Problem]] based on its [[almhirt.validation.Severity]]
     * 
     * @param minSeverity The minimum [[almhirt.validation.Severity]] the [[almhirt.validation.Problem]] must have to be logged
     */
    def log(minSeverity: Severity) {
      logProblem(prob, minSeverity)
    }
    /** Log a [[almhirt.validation.Problem]] */
    def log() {
      logProblem(prob, NoProblem)
    }
  }
  
  /** Implicits to be used on a [[almhirt.validation.AlmValidation]] */
  implicit def almValidation2AlmValidationLoggingW[T](validation: AlmValidation[T]) = new AlmValidationLoggingW[T](validation)
  final class AlmValidationLoggingW[T](validation: AlmValidation[T]) {
    /** Log a [[almhirt.validation.Problem]] based on its [[almhirt.validation.Severity]] in case of a Failure
     * 
     * @param minSeverity The minimum [[almhirt.validation.Severity]] the [[almhirt.validation.Problem]] contained in a Failure must have to be logged
     */
    def logFailure(minSeverity: Severity): AlmValidation[T] = 
      validation fold (prob => {logProblem(prob, minSeverity); validation}, _ => validation )
    /** Log a [[almhirt.validation.Problem]] contained in a Failure */
    def logFailure(): AlmValidation[T] = logFailure(NoProblem)
  }
  
}