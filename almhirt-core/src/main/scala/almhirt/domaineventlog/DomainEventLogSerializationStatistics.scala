package almhirt.domaineventlog

import scala.concurrent.duration._
import almhirt.common._

case class DomainEventLogSerializationStatistics(
  min: FiniteDuration,
  max: FiniteDuration,
  avg: FiniteDuration,
  total: FiniteDuration,
  count: Long,
  serializing: Boolean) {
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
    DomainEventLogSerializationStatistics(newMin, newMax, newAvg, newTotal, newCount, serializing)
  }
  
  override def toString() = {
    s"""DomainEventLogSerializationStatistics(min=$min, max=$max, avg=$avg, total=$total, count=$count, serializing=$serializing)"""
  }
  
  def toNiceString(implicit timeUnitToStringInst: almhirt.converters.FiniteDurationToStringConverter = almhirt.converters.FiniteDurationToStringConverter.default) = {
    val header = if(serializing) "serialization" else "deserialization"
    s"""|$header statistics
     	|min   = ${min.defaultUnitString}
    	|max   = ${max.defaultUnitString}
    	|avg   = ${avg.defaultUnitString}
    	|total = ${total.defaultUnitString}
        |count = $count""".stripMargin
  }
}

object DomainEventLogSerializationStatistics {
  def apply(serializing: Boolean): DomainEventLogSerializationStatistics =
    DomainEventLogSerializationStatistics(Duration.Zero, Duration.Zero, Duration.Zero, Duration.Zero, 0L, serializing)
    
  def forSerializing = DomainEventLogSerializationStatistics(true)  
  def forDeserializing = DomainEventLogSerializationStatistics(false)  
}