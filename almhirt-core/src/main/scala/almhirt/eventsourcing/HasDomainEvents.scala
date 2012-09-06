package almhirt.eventsourcing

import java.util.UUID
import scalaz.Validation
import almhirt._
import almhirt.domain.DomainEvent

trait HasDomainEvents {
  def getAllEvents: AlmFuture[Iterable[DomainEvent]]
  def getEvents(aggRootId: UUID): AlmFuture[Iterable[DomainEvent]]
  def getEvents(aggRootId: UUID, fromVersion: Long): AlmFuture[Iterable[DomainEvent]]
  def getEvents(aggRootId: UUID, fromVersion: Long, toVersion: Long): AlmFuture[Iterable[DomainEvent]]
}