package almhirt.tooling

import almhirt.common._
import almhirt.problem._
import almhirt.tracking.CommandRepresentation

trait Reporter {
  def reportDebug(message: ⇒ String): Unit

  def reportInfo(message: ⇒ String): Unit

  def reportWarning(message: ⇒ String): Unit

  def reportError(message: String, cause: almhirt.problem.ProblemCause): Unit

  def reportRejectedCommand(command: CommandRepresentation, severity: almhirt.problem.Severity, cause: ProblemCause): Unit

  def reportMissedEvent(event: Event, severity: almhirt.problem.Severity, cause: ProblemCause): Unit

  def reportFailure(cause: ProblemCause, severity: almhirt.problem.Severity): Unit

  final def reportMinorFailure(failure: ProblemCause): Unit = reportFailure(failure, MinorSeverity)

  final def reportMajorFailure(failure: ProblemCause): Unit = reportFailure(failure, MajorSeverity)

  final def reportCriticalFailure(failure: ProblemCause): Unit = reportFailure(failure, CriticalSeverity)

  def report(message: ⇒ String, importance: Importance): Unit

  final def reportNotWorthMentioning(message: ⇒ String): Unit = report(message, Importance.NotWorthMentioning)

  final def reportMentionable(message: ⇒ String): Unit = report(message, Importance.Mentionable)

  final def reportImportant(message: ⇒ String): Unit = report(message, Importance.Important)

  final def reportVeryImportant(message: ⇒ String): Unit = report(message, Importance.VeryImportant)

}

//trait ReporterFactory {
//  def createReporter(forComponent: ComponentId)
//}

object Reporter {
  val DevNull = new Reporter {
    def report(message: ⇒ String, importance: Importance): Unit = {}

    def reportDebug(message: ⇒ String): Unit = {}

    def reportError(message: String, cause: ProblemCause): Unit = {}

    def reportFailure(cause: ProblemCause, severity: Severity): Unit = {}

    def reportInfo(message: ⇒ String): Unit = {}

    def reportMissedEvent(event: Event, severity: Severity, cause: ProblemCause): Unit = {}

    def reportRejectedCommand(command: CommandRepresentation, severity: Severity, cause: ProblemCause): Unit = {}

    def reportWarning(message: ⇒ String): Unit = {}
  }
}

trait CompositeReporter extends Reporter {
  def addReporter(rep: Reporter): Unit
}

object CompositeReporter {
  def apply(initial: Reporter*) = new MyCompositeReporter(initial.toList)
}

private[tooling] class MyCompositeReporter(initial: List[Reporter]) extends CompositeReporter {
  private[this] var reporters = initial

  def report(message: ⇒ String, importance: Importance): Unit = {
    reporters.foreach { _.report(message, importance) }
  }

  def reportDebug(message: ⇒ String): Unit = {
    reporters.foreach { _.reportDebug(message) }
  }

  def reportError(message: String, cause: ProblemCause): Unit = {
    reporters.foreach { _.reportError(message, cause) }
  }

  def reportFailure(cause: ProblemCause, severity: Severity): Unit = {
    reporters.foreach { _.reportFailure(cause, severity) }
  }

  def reportInfo(message: ⇒ String): Unit = {
    reporters.foreach { _.reportInfo(message) }
  }

  def reportMissedEvent(event: Event, severity: Severity, cause: ProblemCause): Unit = {
    reporters.foreach { _.reportMissedEvent(event, severity, cause) }
  }

  def reportRejectedCommand(command: CommandRepresentation, severity: Severity, cause: ProblemCause): Unit = {
    reporters.foreach { _.reportRejectedCommand(command, severity, cause) }
  }

  def reportWarning(message: ⇒ String): Unit = {
    reporters.foreach { _.reportWarning(message) }
  }

  def addReporter(rep: Reporter): Unit = {
    reporters = rep :: reporters
  }
}