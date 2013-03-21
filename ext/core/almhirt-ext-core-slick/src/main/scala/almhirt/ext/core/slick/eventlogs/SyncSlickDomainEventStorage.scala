package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.DomainEvent
import almhirt.eventlog.util.SyncDomainEventStorage
import almhirt.serialization._

abstract class SyncSlickDomainEventStorage[TRow <: DomainEventLogRow](
  dal: DomainEventLogStoreComponent[TRow],
  serializer: CanSerializeToFixedChannel[DomainEvent] { type SerializedRepr = TRow#Repr },
  deserializer: CanDeserialize[DomainEvent] { type SerializedRepr = TRow#Repr }) extends SyncDomainEventStorage {

  def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  override def storeEvent(event: DomainEvent): AlmValidation[DomainEvent] = {
    serializer.serialize(event, None).flatMap {
      case (typeIdent, serializedEvent) =>
        val row = createRow(serializer.channel, typeIdent, event, serializedEvent)
        dal.insertEventRow(row)
    }.map(_ => event)
  }

  override def storeManyEvents(events: IndexedSeq[DomainEvent]): (IndexedSeq[DomainEvent], Option[(Problem, IndexedSeq[DomainEvent])]) = {
    import scalaz._, Scalaz._
    (for {
      rows <- events.map(event =>
        serializer.serialize(event, None).map {
          case (typeIdent, serializedEvent) =>
            createRow(serializer.channel, typeIdent, event, serializedEvent)
        }.toAgg).toVector.sequence
      stored <- dal.insertManyEventRows(rows)
    } yield stored).fold(
      fail => (Vector.empty, Some((fail, events))),
      succ => (events, None))
  }

  override def getEventById(id: JUUID): AlmValidation[DomainEvent] =
    for {
      serialized <- dal.getEventRowById(id).map(unpackRow)
      deserialized <- deserializer.deserialize(serialized._2)(serialized._1, Some(serialized._3))
    } yield deserialized

  override def getAllEvents(): AlmValidation[Vector[DomainEvent]] =
    dal.getAllEventRows.flatMap(deserializeManyRows)

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
    rows.map(unpackRow).map(serialized => deserializer.deserialize(serialized._2)(serialized._1, Some(serialized._3)).toAgg).toVector.sequence
  }
}

class SyncTextSlickDomainEventStorage(
  dal: DomainEventLogStoreComponent[TextDomainEventLogRow],
  serializer: CanSerializeToFixedChannel[DomainEvent] { type SerializedRepr = String },
  deserializer: CanDeserialize[DomainEvent] { type SerializedRepr = String }) extends SyncSlickDomainEventStorage[TextDomainEventLogRow](dal, serializer, deserializer) {

  override def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: String): TextDomainEventLogRow =
    TextDomainEventLogRow(event.header.id, event.header.aggRef.id, event.header.aggRef.version, event.header.timestamp, channel, typeIdent, serializedEvent)
  override def unpackRow(row: TextDomainEventLogRow): (String, String, String) =
    (row.payload, row.channel, row.eventtype)
}
    
class SyncBinarySlickDomainEventStorage(
  dal: DomainEventLogStoreComponent[BinaryDomainEventLogRow],
  serializer: CanSerializeToFixedChannel[DomainEvent] { type SerializedRepr = Array[Byte] },
  deserializer: CanDeserialize[DomainEvent] { type SerializedRepr = Array[Byte] }) extends SyncSlickDomainEventStorage[BinaryDomainEventLogRow](dal, serializer, deserializer) {

  override def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: Array[Byte]): BinaryDomainEventLogRow =
    BinaryDomainEventLogRow(event.header.id, event.header.aggRef.id, event.header.aggRef.version, event.header.timestamp, channel, typeIdent, serializedEvent)
  override def unpackRow(row: BinaryDomainEventLogRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.eventtype)
}