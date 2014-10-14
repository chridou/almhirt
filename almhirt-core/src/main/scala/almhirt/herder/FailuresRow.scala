package almhirt.herder

import org.joda.time.LocalDateTime
import almhirt.problem.{ Severity, ProblemCause, Minor }

final case class FailuresEntry(
  totalFailures: Int,
  maxSeverity: Severity,
  summaryQueue: Seq[(ProblemCause, Severity, LocalDateTime)])

object FailuresEntry {
  def apply(): FailuresEntry = FailuresEntry(0, Minor, Vector.empty)

  implicit class FailuresEntryOps(self: FailuresEntry) {
    def add(failure: ProblemCause, severity: almhirt.problem.Severity, timestamp: LocalDateTime, maxInQueue: Int): FailuresEntry = {
      val newTotal = self.totalFailures + 1
      val newSeverity = self.maxSeverity and severity
      val newQueue =
        if (self.summaryQueue.length == maxInQueue)
          (failure, severity, timestamp) +: self.summaryQueue.init
        else
          (failure, severity, timestamp) +: self.summaryQueue
      FailuresEntry(newTotal, newSeverity, newQueue)
    }
  }
}