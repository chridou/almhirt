package almhirt.eventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent
import java.util.{UUID => JUUID}

trait SyncDomainEventStorage {
  def storeEvent(event: DomainEvent): AlmValidation[DomainEvent]
  def storeEvents(events: IndexedSeq[DomainEvent]): (IndexedSeq[DomainEvent], Option[(Problem,IndexedSeq[DomainEvent])])
  def getEventById(id: JUUID): AlmValidation[DomainEvent]
  def getAllEvents(): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsFor(aggId: JUUID): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsForFrom(aggId: JUUID, fromVersion: Long): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsForTo(aggId: JUUID, toVersion: Long): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long): AlmValidation[Iterable[DomainEvent]]
}

trait AsyncDomainEventStorage extends CanStoreDomainEvents with HasDomainEvents {
  def storeEvent(eventLogRow: DomainEvent): AlmFuture[DomainEvent]
}