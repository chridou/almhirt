package almhirt.domaineventlog

case class DomainEventLogWriteStatistics(
	minMs: Long,
	maxMs: Long,
	avgMs: Long,
	totalMs: Long,
	count: Long
) {
  def add(ms: Long) = {
    val newCount = count + 1L
    val newTotal = totalMs + ms 
    val newAvg = newTotal / newCount
    val newMin = 
      if(count == 0L)
        Math.min(ms, Long.MaxValue)
      else
        Math.min(ms, minMs)
    val newMax = Math.min(ms, maxMs)
    DomainEventLogWriteStatistics(newMin, newMax, newAvg, newTotal, newCount)
  }
}

object DomainEventLogWriteStatistics {
  def apply(): DomainEventLogWriteStatistics =
    DomainEventLogWriteStatistics(0, 0, 0, 0, 0)
}