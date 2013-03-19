package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent
import almhirt.eventlog.util.SyncDomainEventStorage

class SyncSlickDomainEventStorage[TRow <: DomainEventLogRow](
  dal: DomainEventLogStoreComponent[TRow] with BlobStoreComponent,
  serialize: DomainEvent => AlmValidation[(String, String, TRow#Repr)],
  deserialize: (TRow#Repr, String, String) => AlmValidation[DomainEvent]) extends SyncDomainEventStorage {
  override def storeEvent(eventLogRow: DomainEvent): AlmValidation[DomainEvent] =
    ???

  override def storeManyEvents(eventLogRows: Seq[DomainEvent]): (IndexedSeq[DomainEvent], Option[(Problem, IndexedSeq[DomainEvent])]) =
    ???

  override def getEventById(id: JUUID): AlmValidation[DomainEvent] =
    ???

  override def getAllEvents(): AlmValidation[Iterable[DomainEvent]] =
    ???

  override def getAllEventsFor(aggId: JUUID): AlmValidation[Iterable[DomainEvent]] =
    ???

  override def getAllEventsForFrom(aggId: JUUID, fromVersion: Long): AlmValidation[Iterable[DomainEvent]] =
    ???

  override def getAllEventsForTo(aggId: JUUID, toVersion: Long): AlmValidation[Iterable[DomainEvent]] =
    ???

  override def getAllEventsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long): AlmValidation[Iterable[DomainEvent]] =
    ???

}