package almhirt.herder

import org.joda.time.LocalDateTime
import almhirt.common.CanCreateDateTime
import almhirt.collections.CircularBuffer

final case class RuntimeHistoryEntry(
  timestamp: LocalDateTime,
  // The amount of free memory in the Java Virtual Machine.
  freeMemory: Long,
  // The total amount of memory in the Java virtual machine. The value returned by this method may vary over time, depending on the host environment.
  totalMemory: Long,
  // The maximum amount of memory that the Java virtual machine will attempt to use. If there is no inherent limit then the value Long.MAX_VALUE will be returned. 
  maxMemory: Long) {

  def usedMemoryRelative: Double =
    freeMemory.toDouble / totalMemory

  def usedMemoryAbsolute: Double =
    freeMemory.toDouble / maxMemory

  def niceString(): String =
    s"""|Free memory: ${Math.round(freeMemory.toDouble / 1000000.0)}MB (${Math.round(freeMemory.toDouble / 1048576.0)}MiB)
       |Total memory: ${Math.round(totalMemory.toDouble / 1000000.0)}MB (${Math.round(totalMemory.toDouble / 1048576.0)}MiB)
       |Max memory: ${Math.round(maxMemory.toDouble / 1000000.0)}MB (${Math.round(maxMemory.toDouble / 1048576.0)}MiB)
       |Used(relative): ${(usedMemoryRelative * 100.0)}
       |Used(absolute): ${(usedMemoryAbsolute * 100.0)}""".stripMargin
}

object RuntimeHistoryEntry {
  def apply(runtime: Runtime)(implicit ccuad: CanCreateDateTime): RuntimeHistoryEntry =
    RuntimeHistoryEntry(
      ccuad.getUtcTimestamp,
      runtime.freeMemory(),
      runtime.totalMemory(),
      runtime.maxMemory())
}

/**
 * maxMemory:
 */
class MutableRuntimeHistory(val maxQueueSize: Int) {
  private var occurencesCount: Int = 0
  private var lastOccurencesQueue = new CircularBuffer[RuntimeHistoryEntry](maxQueueSize)

  def add(what: RuntimeHistoryEntry) {
    occurencesCount += 1
    lastOccurencesQueue.push(what)
  }

  def clear() {
    occurencesCount = 0
    lastOccurencesQueue.clear
  }

  def resize(newSize: Int) {
    lastOccurencesQueue = lastOccurencesQueue.resize(newSize)
  }

  def entries: Vector[RuntimeHistoryEntry] = lastOccurencesQueue.toVector
  def entriesReversed: Vector[RuntimeHistoryEntry] = lastOccurencesQueue.toVector.reverse

  def oldestEntry: Option[RuntimeHistoryEntry] =
    lastOccurencesQueue.headOption

  def latestEntry: Option[RuntimeHistoryEntry] =
    lastOccurencesQueue.lastOption

}
