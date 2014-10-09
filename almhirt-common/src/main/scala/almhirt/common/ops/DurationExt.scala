package almhirt.common.ops

import scala.concurrent.duration._

trait DeadlineExt {
  implicit class DeadlineOps(self: Deadline) {
    def lap: FiniteDuration = Deadline.now - self
    def lapExceeds(dur: FiniteDuration): Boolean = (Deadline.now - self) > dur
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

    def timeUnitString(timeUnit: TimeUnit, round: Option[Int]): String = {
      val t = self.toUnit(timeUnit)
      val s = round.map(r => Math.round(t)).getOrElse(t)
      s"$s[${timeUnitToString(timeUnit)}]"
    }

    def daysString(round: Option[Int]): String = timeUnitString(DAYS, round)
    def hoursString(round: Option[Int]): String = timeUnitString(HOURS, round)
    def minutesString(round: Option[Int]): String = timeUnitString(MINUTES, round)
    def secondsString(round: Option[Int]): String = timeUnitString(SECONDS, round)
    def millisecondsString(round: Option[Int]): String = timeUnitString(MILLISECONDS, round)
    def microsecondsString(round: Option[Int]): String = timeUnitString(MICROSECONDS, round)
    def nanosecondsString(round: Option[Int]): String = timeUnitString(NANOSECONDS, round)

    def exceeds(dur: FiniteDuration): Boolean = self > dur
  }
}