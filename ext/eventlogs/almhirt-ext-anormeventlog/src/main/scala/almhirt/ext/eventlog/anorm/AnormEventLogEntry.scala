package almhirt.ext.eventlog.anorm

import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.DomainEvent
import almhirt.environment._
import riftwarp.RiftWarp
import riftwarp.RiftJson
import riftwarp.DimensionCord
import almhirt.core.CanCreateUuidsAndDateTimes

case class AnormEventLogEntry(id: UUID, aggId: UUID, aggVersion: Long, timestamp: DateTime, payload: scalaz.Cord)

object AnormEventLogEntry{
  def fromDomainEvent(event: DomainEvent)(implicit riftWarp: RiftWarp, timeProvider: CanCreateUuidsAndDateTimes): AlmValidation[AnormEventLogEntry] = {
    val serializedEvent= riftWarp.prepareForWarp[DimensionCord](RiftJson())(event)
    serializedEvent.map(serEvent =>
      AnormEventLogEntry(event.id, event.aggId, event.aggVersion, timeProvider.getDateTime, serEvent.manifestation))
  }
  
  def fromDomainEvents(events: IndexedSeq[DomainEvent])(implicit riftWarp: RiftWarp, timeProvider: CanCreateUuidsAndDateTimes): AlmValidation[IndexedSeq[AnormEventLogEntry]] = {
    val eventsV = events.toList.map(event => fromDomainEvent(event).toAgg).sequence
    eventsV.map(_.toVector)
  }
}
