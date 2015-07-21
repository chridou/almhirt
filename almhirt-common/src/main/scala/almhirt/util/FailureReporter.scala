package almhirt.util

trait FailureReporter {
  def reportFailure(cause: almhirt.problem.ProblemCause, severity: Severity): Unit
}