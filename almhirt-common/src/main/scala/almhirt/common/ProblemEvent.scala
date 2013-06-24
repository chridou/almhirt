package almhirt.common

import almhirt.problem._

final case class ProblemEvent(header: EventHeader, problem: Problem, severity: Severity, category: ProblemCategory) extends Event

object ProblemEvent {
  def apply(problem: Problem, severity: Severity, category: ProblemCategory, sender: Option[String])(implicit ccuad: CanCreateUuidsAndDateTimes): ProblemEvent =
    ProblemEvent(EventHeader(ccuad.getUuid, ccuad.getDateTime, sender), problem, severity, category)
}