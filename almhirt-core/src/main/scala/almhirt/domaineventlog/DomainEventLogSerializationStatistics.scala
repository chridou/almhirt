package almhirt.domaineventlog

case class DomainEventLogSerializationStatistics(
  minMs: Long,
  maxMs: Long,
  avgMs: Long,
  totalMs: Long,
  count: Long,
  count0Ms: Long) {
  def add(ms: Long) = {
    val newCount = count + 1L
    val newCount0Ms =
      if (ms == 0L)
        count0Ms + 1L
      else
        count0Ms
    val newTotal = totalMs + ms
    val newAvg = newTotal / newCount
    val newMin =
      if (newCount == 1L)
        Math.min(ms, Long.MaxValue)
      else
        Math.min(ms, minMs)
    val newMax = Math.max(ms, maxMs)
    DomainEventLogSerializationStatistics(newMin, newMax, newAvg, newTotal, newCount, newCount0Ms)
  }

  override def toString() = {
    val values = tailString()
    s"""DomainEventLogSerializationStatistics$values"""
  }
  
  def tailString() = {
    s"""(minMs=$minMs, maxMs=$maxMs, avgMs=$avgMs, totalMs=$totalMs, count=$count, count0Ms=$count0Ms)"""
  }
}

object DomainEventLogSerializationStatistics {
  def apply(): DomainEventLogSerializationStatistics =
    DomainEventLogSerializationStatistics(0L, 0L, 0L, 0L, 0L, 0L)
}