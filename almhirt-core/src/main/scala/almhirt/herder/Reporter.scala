package almhirt.herder

import almhirt.common._

trait Reporter {
  def report: AlmFuture[Any]
}

object Reporter {
  def apply(getReport: () ⇒ AlmFuture[Any]): Reporter =
    new Reporter {
      def report = getReport()
    }
}