package almhirt.domaineventlog

import scala.concurrent.duration._

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

  def toNiceString(timeUnit: TimeUnit = MILLISECONDS) =
    s"""|read statistics
     	|min   = ${min.toUnit(timeUnit)}
    	|max   = ${max.toUnit(timeUnit)}
    	|avg   = ${avg.toUnit(timeUnit)}
    	|total = ${total.toUnit(timeUnit)}
        |count = $count""".stripMargin
}

object DomainEventLogReadStatistics {
  def apply(): DomainEventLogReadStatistics =
    DomainEventLogReadStatistics(Duration.Zero, Duration.Zero, Duration.Zero, Duration.Zero, 0L)
}