package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.DomainEvent
import almhirt.eventlog.util.SyncDomainEventStorage
import almhirt.serialization._

abstract class SyncSlickDomainEventStorage[TRow <: DomainEventLogRow](
  dal: DomainEventLogStoreComponent[TRow],
  serializer: Serializer[DomainEvent] { type SerializedRepr = TRow#Repr },
  deserializer: Deserializer[DomainEvent] { type SerializedRepr = TRow#Repr }) extends SyncDomainEventStorage {

  def createRow(channel: String, typeIdent: String, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  override def storeEvent(event: DomainEvent): AlmValidation[DomainEvent] = {
    serializer.serialize(event).flatMap {
      case (channel, typeIdent, serializedEvent) =>
        val row = createRow(channel, typeIdent, serializedEvent)
        dal.insertEventRow(row)
    }.map(_ => event)
  }

  override def storeManyEvents(events: IndexedSeq[DomainEvent]): (IndexedSeq[DomainEvent], Option[(Problem, IndexedSeq[DomainEvent])]) = {
    import scalaz._, Scalaz._
    (for {
      rows <- events.map(event =>
        serializer.serialize(event).toAgg).toVector.sequence.map(serializedEvents =>
        serializedEvents.map { case (channel, typeIdent, serializedEvent) => createRow(channel, typeIdent, serializedEvent) })
      stored <- dal.insertManyEventRows(rows)
    } yield stored).fold(
      fail => (Vector.empty, Some((fail, events))),
      succ => (events, None))
  }

  override def getEventById(id: JUUID): AlmValidation[DomainEvent] =
    for {
      serialized <- dal.getEventRowById(id).map(unpackRow)
      deserialized <- deserializer.deserialize(serialized._1, serialized._2, serialized._3)
    } yield deserialized

  override def getAllEvents(): AlmValidation[Vector[DomainEvent]] =
    ???

  override def getAllEventsFor(aggId: JUUID): AlmValidation[Vector[DomainEvent]] =
    dal.getAllEventRowsFor(aggId).flatMap(deserializeManyRows)

  override def getAllEventsForFrom(aggId: JUUID, fromVersion: Long): AlmValidation[Vector[DomainEvent]] =
    dal.getAllEventRowsForFrom(aggId, fromVersion).flatMap(deserializeManyRows)

  override def getAllEventsForTo(aggId: JUUID, toVersion: Long): AlmValidation[Vector[DomainEvent]] =
    dal.getAllEventRowsForTo(aggId, toVersion).flatMap(deserializeManyRows)

  override def getAllEventsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long): AlmValidation[Vector[DomainEvent]] =
    dal.getAllEventRowsForFromTo(aggId, fromVersion, toVersion).flatMap(deserializeManyRows)

  private def deserializeManyRows(rows: Iterable[TRow]): AlmValidation[Vector[DomainEvent]] = {
    import scalaz._, Scalaz._
    rows.map(unpackRow).map(serialized => deserializer.deserialize(serialized._1, serialized._2, serialized._3).toAgg).toVector.sequence
  }
}