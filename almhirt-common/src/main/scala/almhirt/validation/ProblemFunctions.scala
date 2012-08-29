package almhirt.validation

trait ProblemFunctions {
  def badData(key: String, message: String) =
    SingleBadDataProblem(message, key)
  def systemProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) =
    UnspecifiedProblem(message, severity, SystemProblem, exception, args)
  def applicationProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) =
    UnspecifiedProblem(message, severity, ApplicationProblem, exception, args)
}

object ProblemFunctions extends ProblemFunctions