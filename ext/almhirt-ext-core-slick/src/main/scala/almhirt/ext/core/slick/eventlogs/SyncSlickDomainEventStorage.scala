package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.DomainEvent
import almhirt.eventlog.SyncDomainEventStorage
import almhirt.serialization._

abstract class SyncSlickDomainEventStorage[TRow <: DomainEventLogRow](
  dal: DomainEventLogStoreComponent[TRow],
  serializing: CanSerializeToFixedChannelAndDeserialize[DomainEvent, DomainEvent] { type SerializedRepr = TRow#Repr }) extends SyncDomainEventStorage {

  def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  override final def storeEvent(event: DomainEvent): AlmValidation[DomainEvent] = {
    serializing.serialize(event).flatMap {
      case (serializedEvent, Some(ti)) =>
        val row = createRow(serializing.channel, ti, event, serializedEvent)
          dal.insertEventRow(row)
      case (_, None) => UnspecifiedProblem("A type identifier is required.").failure
    }.map(_ => event)
  }

  override final def storeEvents(events: IndexedSeq[DomainEvent]): (IndexedSeq[DomainEvent], Option[(Problem, IndexedSeq[DomainEvent])]) = {
    import scalaz._, Scalaz._
    (for {
      rows <- events.map(event =>
        serializing.serialize(event).flatMap {
          case (serializedEvent, Some(ti)) =>
            (createRow(serializing.channel, ti, event, serializedEvent)).success
          case (_, None) => UnspecifiedProblem("A type identifier is required.").failure
        }.toAgg).toVector.sequence
      stored <- dal.insertManyEventRows(rows)
    } yield stored).fold(
      fail => (Vector.empty, Some((fail, events))),
      succ => (events, None))
  }

  override def getEventById(id: JUUID): AlmValidation[DomainEvent] =
    for {
      serialized <- dal.getEventRowById(id).map(unpackRow)
      deserialized <- serializing.deserialize(serialized._2)(serialized._1)
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
    rows.map(unpackRow).map(serialized => serializing.deserialize(serialized._2)(serialized._1).toAgg).toVector.sequence
  }
}

class SyncTextSlickDomainEventStorage(
  dal: DomainEventLogStoreComponent[TextDomainEventLogRow],
  serializing: StringSerializingToFixedChannel[DomainEvent, DomainEvent]) extends SyncSlickDomainEventStorage[TextDomainEventLogRow](dal, serializing) {

  override def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: String): TextDomainEventLogRow =
    TextDomainEventLogRow(event.header.id, event.header.aggRef.id, event.header.aggRef.version, event.header.timestamp, typeIdent, channel, serializedEvent)
  override def unpackRow(row: TextDomainEventLogRow): (String, String, String) =
    (row.payload, row.channel, row.eventtype)
}

class SyncBinarySlickDomainEventStorage(
  dal: DomainEventLogStoreComponent[BinaryDomainEventLogRow],
  serializing: BinarySerializingToFixedChannel[DomainEvent, DomainEvent]) extends SyncSlickDomainEventStorage[BinaryDomainEventLogRow](dal, serializing) {

  override def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: Array[Byte]): BinaryDomainEventLogRow =
    BinaryDomainEventLogRow(event.header.id, event.header.aggRef.id, event.header.aggRef.version, event.header.timestamp, channel, typeIdent, serializedEvent)
  override def unpackRow(row: BinaryDomainEventLogRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.eventtype)
}