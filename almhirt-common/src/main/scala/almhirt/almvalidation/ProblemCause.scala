package almhirt.validation

sealed trait ProblemCause
case class CauseIsThrowable(exn: Throwable) extends ProblemCause
case class CauseIsProblem(prob: Problem) extends ProblemCause
