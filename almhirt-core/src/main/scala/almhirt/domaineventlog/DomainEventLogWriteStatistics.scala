package almhirt.domaineventlog

import scala.concurrent.duration._
import almhirt.common._

case class DomainEventLogWriteStatistics(
  min: FiniteDuration,
  max: FiniteDuration,
  avg: FiniteDuration,
  total: FiniteDuration,
  count: Long,
  noOps: Long) {
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
    DomainEventLogWriteStatistics(newMin, newMax, newAvg, newTotal, newCount, noOps)
  }
  
  def addNoOp() = copy(noOps = this.noOps + 1L)

  override def toString() = {
    s"""DomainEventLogWriteStatistics(min=$min, max=$max, avg=$avg, total=$total, count=$count, noOps=$noOps)"""
  }
 
  def toNiceString(implicit timeUnitToStringInst: almhirt.converters.FiniteDurationToStringConverter = almhirt.converters.FiniteDurationToStringConverter.default) =
    s"""|write statistics
     	|min   = ${min.defaultUnitString}
    	|max   = ${max.defaultUnitString}
    	|avg   = ${avg.defaultUnitString}
    	|total = ${total.defaultUnitString}
        |count = $count
        |noOps = $noOps""".stripMargin
  
}

object DomainEventLogWriteStatistics {
  def apply(): DomainEventLogWriteStatistics =
    DomainEventLogWriteStatistics(Duration.Zero, Duration.Zero, Duration.Zero, Duration.Zero, 0L, 0L)
}