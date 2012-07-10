package almhirt.eventsourcing

import java.util.UUID
import scalaz.Validation
import almhirt.validation.Problem
import almhirt.concurrent.AlmFuture
import almhirt.domain.EntityEvent

trait HasEntityEvents {
  def getAllEvents: AlmFuture[Iterable[EntityEvent]]
  def getEvents(entityId: UUID): AlmFuture[Iterable[EntityEvent]]
  def getEvents(entityId: UUID, fromVersion: Long): AlmFuture[Iterable[EntityEvent]]
  def getEvents(entityId: UUID, fromVersion: Long, toVersion: Long): AlmFuture[Iterable[EntityEvent]]
}