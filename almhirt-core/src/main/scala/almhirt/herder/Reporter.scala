package almhirt.herder

import almhirt.common._

trait StatusReporter {
  def description: Option[String]
  def report(options: ezreps.EzOptions): AlmFuture[ezreps.EzReport]
}

object StatusReporter {
  def apply(getReport: ezreps.EzOptions ⇒ AlmFuture[ezreps.EzReport]): StatusReporter =
    make(getReport, None)

  def apply(getReport: ezreps.EzOptions ⇒ AlmFuture[ezreps.EzReport], description: String): StatusReporter =
    make(getReport, Some(description))

  def make(getReport: ezreps.EzOptions ⇒ AlmFuture[ezreps.EzReport], aDescription: Option[String]): StatusReporter =
    new StatusReporter {
      def description = aDescription
      def report(options: ezreps.EzOptions) = getReport(options)
    }
}