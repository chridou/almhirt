package almhirt.eventlog.anorm

import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.DomainEvent
import almhirt.environment.AlmhirtContext
import almhirt.riftwarp.RiftJson

case class AnormEventLogEntry(id: UUID, version: Long, timestamp: DateTime, payload: String)

object AnormEventLogEntry{
  def fromDomainEvent(event: DomainEvent)(implicit ctx: AlmhirtContext): AlmValidation[AnormEventLogEntry] = {
    val serializedEvent = ctx.riftWarp.prepareForWarp(RiftJson)(event)
    serializedEvent.map(serEvent =>
      AnormEventLogEntry(event.id, event.version, ctx.getDateTime, serEvent))
  }
  
  def fromDomainEvents(events: List[DomainEvent])(implicit ctx: AlmhirtContext): AlmValidation[List[AnormEventLogEntry]] =
    events.map(event => fromDomainEvent(event).toAgg).sequence
}
