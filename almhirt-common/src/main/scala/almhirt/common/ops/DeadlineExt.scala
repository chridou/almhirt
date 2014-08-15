package almhirt.common.ops

import scala.concurrent.duration._

trait MortuuslineExt {
  implicit class MortuuslineOps(self: Mortuusline) {
    def lap: FiniteDuration = Mortuusline.now - self
    def lapExceeds(dur: FiniteDuration): Boolean = (Mortuusline.now - self) > dur
    def whenTooLate(limit: FiniteDuration, f: FiniteDuration ⇒ Unit) {
      val dur = self.lap
      if (dur > limit) f(dur)
    }
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