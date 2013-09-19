package almhirt.domaineventlog

import scala.concurrent.duration._

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
  
  def toNiceString(timeUnit: TimeUnit = MILLISECONDS) = {
    val header = if(serializing) "serialization" else "deserialization"
    s"""|$header statistics
     	|min   = ${min.toUnit(timeUnit)}
    	|max   = ${max.toUnit(timeUnit)}
    	|avg   = ${avg.toUnit(timeUnit)}
    	|total = ${total.toUnit(timeUnit)}
        |count = $count""".stripMargin
  }
}

object DomainEventLogSerializationStatistics {
  def apply(serializing: Boolean): DomainEventLogSerializationStatistics =
    DomainEventLogSerializationStatistics(Duration.Zero, Duration.Zero, Duration.Zero, Duration.Zero, 0L, serializing)
    
  def forSerializing = DomainEventLogSerializationStatistics(true)  
  def forDeserializing = DomainEventLogSerializationStatistics(false)  
}