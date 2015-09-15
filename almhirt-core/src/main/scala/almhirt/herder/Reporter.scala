package almhirt.herder

import almhirt.common._

trait StatusReporter {
  def report: AlmFuture[almhirt.akkax.reporting.StatusReport]
}

object StatusReporter {
  def apply(getReport: () ⇒ AlmFuture[almhirt.akkax.reporting.StatusReport]): StatusReporter =
    new StatusReporter {
      def report = getReport()
    }
}