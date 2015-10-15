package almhirt.herder

import almhirt.akkax.events.ComponentEvent
import almhirt.common._
import almhirt.akkax.GlobalComponentId

final case class RuntimeStateRecorded(header: EventHeader, origin: GlobalComponentId,
                                      freeMemory: Long,
                                      usedMemory: Long,
                                      totalMemory: Long,
                                      maxMemory: Long) extends ComponentEvent
object RuntimeStateRecorded {
  def apply(freeMemory: Long,
            usedMemory: Long,
            totalMemory: Long,
            maxMemory: Long)(header: EventHeader, origin: GlobalComponentId): RuntimeStateRecorded =
    RuntimeStateRecorded(header, origin, freeMemory, usedMemory, totalMemory, maxMemory)
  def apply(historyEntry: RuntimeHistoryEntry)(header: EventHeader, origin: GlobalComponentId): RuntimeStateRecorded =
    RuntimeStateRecorded(header, origin, historyEntry.freeMemory, historyEntry.usedMemory, historyEntry.totalMemory, historyEntry.maxMemory)
}