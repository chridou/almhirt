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