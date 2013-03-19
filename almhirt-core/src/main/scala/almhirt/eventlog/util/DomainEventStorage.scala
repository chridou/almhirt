package almhirt.eventlog.util

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent

trait SyncDomainEventStorage {
  def storeEvent(eventLogRow: DomainEvent): AlmValidation[DomainEvent]
  def storeManyEvents(eventLogRows: Seq[DomainEvent]): (IndexedSeq[DomainEvent], Option[(Problem, IndexedSeq[DomainEvent])])
  def getEventById(id: JUUID): AlmValidation[DomainEvent]
  def getAllEvents(): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsFor(aggId: JUUID): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsForFrom(aggId: JUUID, fromVersion: Long): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsForTo(aggId: JUUID, toVersion: Long): AlmValidation[Iterable[DomainEvent]]
  def getAllEventsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long): AlmValidation[Iterable[DomainEvent]]
}