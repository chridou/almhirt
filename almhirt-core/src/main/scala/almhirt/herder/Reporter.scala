package almhirt.herder

import almhirt.common._

trait Report {
  def name: String
}

trait Reporter {
  def report: AlmFuture[Report]
}

object Reporter {
  def apply(getReport: () ⇒ AlmFuture[Report]): Reporter =
    new Reporter {
      def report = getReport()
    }
}