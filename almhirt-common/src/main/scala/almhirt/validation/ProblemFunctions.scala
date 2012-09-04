package almhirt.validation

trait ProblemFunctions {
  def badData(key: String, message: String) =
    SingleBadDataProblem(message, key)
  def systemProblem(message: String, severity: Severity = Major, cause: Option[ProblemCause] = None, args: Map[String, Any] = Map()) =
    UnspecifiedProblem(message, severity, SystemProblem, args, cause)
  def applicationProblem(message: String, severity: Severity = Major, cause: Option[ProblemCause] = None, args: Map[String, Any] = Map()) =
    UnspecifiedProblem(message, severity, ApplicationProblem, args, cause)
}

object ProblemFunctions extends ProblemFunctions