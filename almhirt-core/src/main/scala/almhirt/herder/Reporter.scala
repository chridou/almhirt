package almhirt.herder

import almhirt.common._

trait StatusReport {
  def name: String
}

trait StatusReporter {
  def report: AlmFuture[StatusReport]
}

object StatusReporter {
  def apply(getReport: () ⇒ AlmFuture[StatusReport]): StatusReporter =
    new StatusReporter {
      def report = getReport()
    }
}