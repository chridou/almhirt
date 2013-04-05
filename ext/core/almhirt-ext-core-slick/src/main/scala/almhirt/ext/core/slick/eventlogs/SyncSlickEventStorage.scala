package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.Event
import almhirt.eventlog.util.SyncEventStorage
import almhirt.serialization._
import almhirt.ext.core.slick.TypeConversion._
import almhirt.ext.core.slick.shared.BlobStoreComponent

abstract class SyncSlickEventStorage[TRow <: EventLogRow](
  dal: EventLogStoreComponent[TRow] with BlobStoreComponent with CanStoreEventLogRowWithBlobs[TRow],
  blobPolicy: BlobPolicy,
  serializing: CanSerializeToFixedChannelAndDeserialize[Event, Event] { type SerializedRepr = TRow#Repr }) extends SyncEventStorage {

  def createRow(channel: String, typeIdent: String, event: Event, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  final protected def blobRefsToData(blobRefs: Vector[ExtractedBlobReference]): AlmValidation[Vector[(JUUID, Array[Byte])]] =
    almhirt.almvalidation.funs.inTryCatch {
      blobRefs.map { case ExtractedBlobReference(BlobRefByUuid(uuid), data) => (uuid, data) }
    }.leftMap(prob => MappingProblem("Only a BlobRefByUuid is allowed!", cause = Some(prob)))

  override final def storeEvent(event: Event): AlmValidation[Event] = {
    serializing.serializeBlobSeparating(blobPolicy.serializationPolicy)(event, None).flatMap {
      case (Some(ti), serializedEvent, blobs) =>
        val row = createRow(serializing.channel, ti, event, serializedEvent)
        blobRefsToData(blobs).flatMap(blobData =>
          dal.storeRowAndBlobs(row, blobData))
      case (None, _, _) => UnspecifiedProblem("A type identifier is required.").failure
    }.map(_ => event)
  }

  override def getEventById(id: JUUID): AlmValidation[Event] =
    for {
      serialized <- dal.getEventRowById(id).map(unpackRow)
      deserialized <- serializing.deserializeBlobIntegrating(blobPolicy.deserializationPolicy)(serialized._2)(serialized._1, Some(serialized._3))
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
    rows.map(unpackRow).map(serialized => serializing.deserializeBlobIntegrating(blobPolicy.deserializationPolicy)(serialized._2)(serialized._1, Some(serialized._3)).toAgg).toVector.sequence
  }
}

class SyncTextSlickEventStorage(

  dal: EventLogStoreComponent[TextEventLogRow] with BlobStoreComponent with CanStoreEventLogRowWithBlobs[TextEventLogRow],
  blobPolicy: BlobPolicy,
  serializing: StringSerializingToFixedChannel[Event, Event]) extends SyncSlickEventStorage[TextEventLogRow](dal, blobPolicy, serializing) {

  override def createRow(channel: String, typeIdent: String, event: Event, serializedEvent: String): TextEventLogRow =
    TextEventLogRow(event.header.id, dateTimeToTimeStamp(event.header.timestamp), typeIdent, channel, serializedEvent)
  override def unpackRow(row: TextEventLogRow): (String, String, String) =
    (row.payload, row.channel, row.eventtype)
}

class SyncBinarySlickEventStorage(
  dal: EventLogStoreComponent[BinaryEventLogRow] with BlobStoreComponent with CanStoreEventLogRowWithBlobs[BinaryEventLogRow],
  blobPolicy: BlobPolicy,
  serializing: BinarySerializingToFixedChannel[Event, Event]) extends SyncSlickEventStorage[BinaryEventLogRow](dal, blobPolicy, serializing) {

  override def createRow(channel: String, typeIdent: String, event: Event, serializedEvent: Array[Byte]): BinaryEventLogRow =
    BinaryEventLogRow(event.header.id, dateTimeToTimeStamp(event.header.timestamp), typeIdent, channel, serializedEvent)
  override def unpackRow(row: BinaryEventLogRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.eventtype)
}