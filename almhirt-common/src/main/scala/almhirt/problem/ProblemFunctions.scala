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