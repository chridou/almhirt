package almhirt.herder

import almhirt.common._
import almhirt.akkax.reporting.ReportOptions

trait StatusReporter {
  def description: Option[String]
  def report(options: ReportOptions): AlmFuture[almhirt.akkax.reporting.StatusReport]
}

object StatusReporter {
  def apply(getReport: ReportOptions ⇒ AlmFuture[almhirt.akkax.reporting.StatusReport]): StatusReporter =
    make(getReport, None)

  def apply(getReport: ReportOptions ⇒ AlmFuture[almhirt.akkax.reporting.StatusReport], description: String): StatusReporter =
    make(getReport, Some(description))

  def make(getReport: ReportOptions ⇒ AlmFuture[almhirt.akkax.reporting.StatusReport], aDescription: Option[String]): StatusReporter =
    new StatusReporter {
      def description = aDescription
      def report(options: ReportOptions) = getReport(options)
    }
}