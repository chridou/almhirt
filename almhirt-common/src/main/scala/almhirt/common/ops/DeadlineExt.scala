package almhirt.common.ops

import scala.concurrent.duration._

trait DeadlineExt {
  implicit class DeadlineOps(self: Deadline) {
    def lap: FiniteDuration = Deadline.now - self
    def lapExceeds(dur: FiniteDuration): Boolean = (Deadline.now - self) > dur
  }
}

trait FiniteDurationExt {
  private val timeUnitToString =
    Map(DAYS -> "d",
      HOURS -> "h",
      MINUTES -> "min",
      SECONDS -> "s",
      MILLISECONDS -> "ms",
      MICROSECONDS -> "µs",
      NANOSECONDS -> "ns")


  implicit class FiniteDurationOps(self: FiniteDuration) {
    def defaultUnitString(implicit timeUnitToStringInst: almhirt.converters.FiniteDurationToStringConverter = almhirt.converters.FiniteDurationToStringConverter.default): String = {
      timeUnitToStringInst.convert(self)
    }

    def timeUnitString(timeUnit: TimeUnit): String = {
      s"""${self.toUnit(timeUnit).toString}[${timeUnitToString(timeUnit)}]"""
    }

    def daysString(): String = timeUnitString(DAYS)
    def hoursString(): String = timeUnitString(HOURS)
    def minutesString(): String = timeUnitString(MINUTES)
    def secondsString(): String = timeUnitString(SECONDS)
    def millisecondsString(): String = timeUnitString(MILLISECONDS)
    def microsecondsString(): String = timeUnitString(MICROSECONDS)
    def nanosecondsString(): String = timeUnitString(NANOSECONDS)

    def exceeds(dur: FiniteDuration): Boolean = self > dur
  }
}