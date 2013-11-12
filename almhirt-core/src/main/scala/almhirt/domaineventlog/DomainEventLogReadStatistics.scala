package almhirt.domaineventlog

import scala.concurrent.duration._
import almhirt.common._

case class DomainEventLogReadStatistics(
  min: FiniteDuration,
  max: FiniteDuration,
  avg: FiniteDuration,
  total: FiniteDuration,
  count: Long) {
  def add(duration: FiniteDuration) = {
    val newCount = count + 1L
    val newTotal = total + duration
    val newAvg = newTotal / newCount
    val newMin =
      if (newCount == 1L)
        duration
      else
        duration.min(min)
    val newMax = duration.max(max)
    DomainEventLogReadStatistics(newMin, newMax, newAvg, newTotal, newCount)
  }

  override def toString() = {
    s"""DomainEventLogReadStatistics(min=$min, max=$max, avg=$avg, total=$total, count=$count)"""
  }

  def toNiceString(implicit timeUnitToStringInst: almhirt.converters.FiniteDurationToStringConverter = almhirt.converters.FiniteDurationToStringConverter.default) =
    s"""|read statistics
     	|min   = ${min.defaultUnitString}
    	|max   = ${max.defaultUnitString}
    	|avg   = ${avg.defaultUnitString}
    	|total = ${total.defaultUnitString}
        |count = $count""".stripMargin
}

object DomainEventLogReadStatistics {
  def apply(): DomainEventLogReadStatistics =
    DomainEventLogReadStatistics(Duration.Zero, Duration.Zero, Duration.Zero, Duration.Zero, 0L)
}