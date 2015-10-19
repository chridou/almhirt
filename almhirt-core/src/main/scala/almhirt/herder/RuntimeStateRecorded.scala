package almhirt.herder

import almhirt.akkax.events.ComponentEvent
import almhirt.common._
import almhirt.akkax.GlobalComponentId

final case class RuntimeStateRecorded(header: EventHeader, origin: GlobalComponentId,
                                      freeMemory: Long,
                                      usedMemory: Long,
                                      totalMemory: Long,
                                      maxMemory: Long,
                                      usedFractionFromTotal: Double,
                                      usedFractionFromMax: Double,
                                      systemLoadAverage: Double) extends ComponentEvent
object RuntimeStateRecorded {
  def apply(freeMemory: Long,
            usedMemory: Long,
            totalMemory: Long,
            maxMemory: Long,
            usedFractionFromTotal: Double,
            usedFractionFromMax: Double,
            systemLoadAverage: Double)(header: EventHeader, origin: GlobalComponentId): RuntimeStateRecorded =
    RuntimeStateRecorded(header, origin, freeMemory, usedMemory, totalMemory, maxMemory, usedFractionFromTotal, usedFractionFromMax, systemLoadAverage)
  def apply(historyEntry: RuntimeHistoryEntry)(header: EventHeader, origin: GlobalComponentId): RuntimeStateRecorded =
    RuntimeStateRecorded(header, origin, historyEntry.freeMemory, historyEntry.usedMemory, historyEntry.totalMemory, historyEntry.maxMemory, historyEntry.usedMemoryRelative, historyEntry.usedMemoryAbsolute, historyEntry.systemLoadAverage)

  def empty(header: EventHeader, origin: GlobalComponentId): RuntimeStateRecorded =
    RuntimeStateRecorded(header, origin, 0L, 0L, 0L, 0L, 0.0, 0.0, 0.0)
}