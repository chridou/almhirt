package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import org.joda.time.DateTime
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.eventlog.SyncEventStorage
import almhirt.serialization._
import almhirt.ext.core.slick.TypeConversion._
import almhirt.core.CanPublishEvents

abstract class SyncSlickEventStorage[TRow <: EventLogRow](
  name: String,
  dal: EventLogStoreComponent[TRow],
  support: CanPublishEvents with CanCreateUuidsAndDateTimes,
  logger: LoggingAdapter,
  serializing: CanSerializeToFixedChannelAndDeserialize[Event, Event] { type SerializedRepr = TRow#Repr }) extends SyncEventStorage {

  def createRow(channel: String, typeIdent: String, event: Event, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  final def storeEvent(event: Event): AlmValidation[Event] = {
    serializing.serialize(event, None).flatMap {
      case (Some(ti), serializedEvent) =>
        val row = createRow(serializing.channel, ti, event, serializedEvent)
        dal.insertEventRow(row)
      case (None, _) => UnspecifiedProblem("A type identifier is required.").failure
    }.map(_ => event)
  }

  override def consume(event: Event) {
    if (event.header.sender != Some(name)) {
      val res = storeEvent(event)
      res.onFailure(prob => {
        val event = ProblemEvent(prob, Some(name))(support)
        support.publishEvent(event)
        storeEvent(event).onFailure(prob =>
          logger.error(s"""SyncSlickEventStorage with name "$name" could not store the problem event, it has just created. The Problem was: ${prob.toString}"""))
      })
    }
  }

  override def getEventById(id: JUUID): AlmValidation[Event] =
    for {
      serialized <- dal.getEventRowById(id).map(unpackRow)
      deserialized <- serializing.deserialize(serialized._2)(serialized._1, Some(serialized._3))
    } yield deserialized

  override def getAllEvents(): AlmValidation[Vector[Event]] =
    dal.getAllEventRows.flatMap(deserializeManyRows)

  override def getAllEventsFrom(from: DateTime): AlmValidation[Vector[Event]] =
    dal.getAllEventRowsFrom(dateTimeToTimeStamp(from)).flatMap(deserializeManyRows)

  override def getAllEventsUntil(until: DateTime): AlmValidation[Vector[Event]] =
    dal.getAllEventRowsUntil(dateTimeToTimeStamp(until)).flatMap(deserializeManyRows)

  override def getAllEventsFromUntil(from: DateTime, until: DateTime): AlmValidation[Vector[Event]] =
    dal.getAllEventRowsFromUntil(dateTimeToTimeStamp(from), dateTimeToTimeStamp(until)).flatMap(deserializeManyRows)

  private def deserializeManyRows(rows: Iterable[TRow]): AlmValidation[Vector[Event]] = {
    import scalaz._, Scalaz._
    rows.map(unpackRow).map(serialized => serializing.deserialize(serialized._2)(serialized._1, Some(serialized._3)).toAgg).toVector.sequence
  }
}

class SyncTextSlickEventStorage(
  name: String,
  dal: EventLogStoreComponent[TextEventLogRow],
  support: CanPublishEvents with CanCreateUuidsAndDateTimes,
  logger: LoggingAdapter,
  serializing: StringSerializingToFixedChannel[Event, Event]) extends SyncSlickEventStorage[TextEventLogRow](name, dal, support, logger, serializing) {

  override def createRow(channel: String, typeIdent: String, event: Event, serializedEvent: String): TextEventLogRow =
    TextEventLogRow(event.header.id, event.header.sender, dateTimeToTimeStamp(event.header.timestamp), typeIdent, channel, serializedEvent)
  override def unpackRow(row: TextEventLogRow): (String, String, String) =
    (row.payload, row.channel, row.eventtype)
}

class SyncBinarySlickEventStorage(
  name: String,
  dal: EventLogStoreComponent[BinaryEventLogRow],
  support: CanPublishEvents with CanCreateUuidsAndDateTimes,
  logger: LoggingAdapter,
  serializing: BinarySerializingToFixedChannel[Event, Event]) extends SyncSlickEventStorage[BinaryEventLogRow](name, dal, support, logger, serializing) {

  override def createRow(channel: String, typeIdent: String, event: Event, serializedEvent: Array[Byte]): BinaryEventLogRow =
    BinaryEventLogRow(event.header.id, event.header.sender, dateTimeToTimeStamp(event.header.timestamp), typeIdent, channel, serializedEvent)
  override def unpackRow(row: BinaryEventLogRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.eventtype)
}