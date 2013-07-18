package almhirt.common

import almhirt.common._

trait FailureEvent extends Event with EventTemplate[FailureEvent]{
  /**
   * A short description of the context in which the failure occurred.
   * What where you doing when the failure was discovered?
   */
  def context: String
  def severity: almhirt.problem.Severity
}

object FailureEvent {
  def apply(context: String, severity: almhirt.problem.Severity, problem: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): FailureEvent =
    ???
  def apply(context: String, severity: almhirt.problem.Severity, exn: Throwable)(implicit ccuad: CanCreateUuidsAndDateTimes): FailureEvent =
    ???
}

final case class ProblemOccurred(context: String, severity: almhirt.problem.Severity, problem: Problem) extends FailureEvent with EventTemplate[FailureEvent]
final case class ExceptionOccurred(context: String, severity: almhirt.problem.Severity, exnType: String, exnMessage: String, exnStackTrace: Option[String]) extends FailureEvent with EventTemplate[FailureEvent]