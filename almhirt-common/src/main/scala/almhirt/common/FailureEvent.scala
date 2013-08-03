package almhirt.common

import almhirt.common._

trait FailureEvent extends Event {
  /**
   * A short description of the context in which the failure occurred.
   * What where you doing when the failure was discovered?
   */
  def context: String
  def severity: almhirt.problem.Severity
}

object FailureEvent {
  def apply(context: String, problem: Problem, severity: almhirt.problem.Severity)(implicit ccuad: CanCreateUuidsAndDateTimes): FailureEvent =
    ProblemOccurred(EventHeader(), context, problem, severity)

  def apply(context: String, exn: Throwable, severity: almhirt.problem.Severity)(implicit ccuad: CanCreateUuidsAndDateTimes): FailureEvent =
    ExceptionOccurred(EventHeader(), context, exn.getClass().getName, exn.getMessage, Some(exn.getStackTraceString), severity)
}

final case class ProblemOccurred(header: EventHeader, context: String, problem: Problem, severity: almhirt.problem.Severity) extends FailureEvent {
   override def changeMetadata(newMetaData: Map[String, String]): ProblemOccurred = copy(header = this.header.changeMetadata(newMetaData))
}

final case class ExceptionOccurred(header: EventHeader, context: String, exnType: String, exnMessage: String, exnStackTrace: Option[String], severity: almhirt.problem.Severity) extends FailureEvent {
   override def changeMetadata(newMetaData: Map[String, String]): ExceptionOccurred = copy(header = this.header.changeMetadata(newMetaData))
}