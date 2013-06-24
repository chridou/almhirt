package almhirt.common

import almhirt.problem._

trait ProblemConsumer {
  def consumeProblem(problem: Problem, severity: Severity, category: ProblemCategory, sender: Option[String])
}

trait ProblemConsumingComponent extends ProblemConsumer { self: Consumer[Event] with CanCreateUuidsAndDateTimes =>
  def consumeProblem(problem: Problem, severity: Severity, category: ProblemCategory, sender: Option[String]) {
    self.consume(ProblemEvent(problem, severity, category, sender)(self))
  }
}