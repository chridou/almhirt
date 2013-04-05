package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.DomainEvent
import almhirt.eventlog.util.SyncDomainEventStorage
import almhirt.serialization._
import almhirt.ext.core.slick.shared.{BlobStoreComponent, BlobRow }

abstract class SyncSlickDomainEventStorage[TRow <: DomainEventLogRow](
  dal: DomainEventLogStoreComponent[TRow] with BlobStoreComponent with CanStoreDomainEventLogRowWithBlobs[TRow],
  blobPolicy: BlobPolicy,
  serializing: CanSerializeToFixedChannelAndDeserialize[DomainEvent, DomainEvent] { type SerializedRepr = TRow#Repr }) extends SyncDomainEventStorage {

  def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  final protected def blobRefsToData(blobRefs: Vector[ExtractedBlobReference]): AlmValidation[Vector[(JUUID, Array[Byte])]] =
    almhirt.almvalidation.funs.inTryCatch {
      blobRefs.map { case ExtractedBlobReference(BlobRefByUuid(uuid), data) => (uuid, data) }
    }.leftMap(prob => MappingProblem("Only a BlobRefByUuid is allowed!", cause = Some(prob)))

  override final def storeEvent(event: DomainEvent): AlmValidation[DomainEvent] = {
    serializing.serializeBlobSeparating(blobPolicy.serializationPolicy)(event, None).flatMap {
      case (Some(ti), serializedEvent, blobs) =>
        val row = createRow(serializing.channel, ti, event, serializedEvent)
        blobRefsToData(blobs).flatMap(blobData =>
          dal.storeRowAndBlobs(row, blobData))
      case (None, _, _) => UnspecifiedProblem("A type identifier is required.").failure
    }.map(_ => event)
  }

  override final def storeManyEvents(events: IndexedSeq[DomainEvent]): (IndexedSeq[DomainEvent], Option[(Problem, IndexedSeq[DomainEvent])]) = {
    import scalaz._, Scalaz._
    (for {
      rows <- events.map(event =>
        serializing.serializeBlobSeparating(blobPolicy.serializationPolicy)(event, None).flatMap {
          case (Some(ti), serializedEvent, blobs) =>
            (createRow(serializing.channel, ti, event, serializedEvent), blobs).success
          case (None, _, _) => UnspecifiedProblem("A type identifier is required.").failure
        }.toAgg).toVector.sequence
      rowsWithBlobData <- rows.map{ case (row, blobs) => blobRefsToData(blobs).map((row, _)).toAgg }.sequence
      stored <- dal.storeManyRowsAndBlobs(rowsWithBlobData)
    } yield stored).fold(
      fail => (Vector.empty, Some((fail, events))),
      succ => (events, None))
  }

  override def getEventById(id: JUUID): AlmValidation[DomainEvent] =
    for {
      serialized <- dal.getEventRowById(id).map(unpackRow)
      deserialized <- serializing.deserializeBlobIntegrating(blobPolicy.deserializationPolicy)(serialized._2)(serialized._1, Some(serialized._3))
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
    rows.map(unpackRow).map(serialized => serializing.deserializeBlobIntegrating(blobPolicy.deserializationPolicy)(serialized._2)(serialized._1, Some(serialized._3)).toAgg).toVector.sequence
  }
}

class SyncTextSlickDomainEventStorage(
  dal: DomainEventLogStoreComponent[TextDomainEventLogRow] with BlobStoreComponent with CanStoreDomainEventLogRowWithBlobs[TextDomainEventLogRow],
  blobPolicy: BlobPolicy,
  serializing: StringSerializingToFixedChannel[DomainEvent, DomainEvent]) extends SyncSlickDomainEventStorage[TextDomainEventLogRow](dal, blobPolicy, serializing) {

  override def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: String): TextDomainEventLogRow =
    TextDomainEventLogRow(event.header.id, event.header.aggRef.id, event.header.aggRef.version, event.header.timestamp, typeIdent, channel, serializedEvent)
  override def unpackRow(row: TextDomainEventLogRow): (String, String, String) =
    (row.payload, row.channel, row.eventtype)
}

class SyncBinarySlickDomainEventStorage(
  dal: DomainEventLogStoreComponent[BinaryDomainEventLogRow] with BlobStoreComponent with CanStoreDomainEventLogRowWithBlobs[BinaryDomainEventLogRow],
  blobPolicy: BlobPolicy,
  serializing: BinarySerializingToFixedChannel[DomainEvent, DomainEvent]) extends SyncSlickDomainEventStorage[BinaryDomainEventLogRow](dal, blobPolicy, serializing) {

  override def createRow(channel: String, typeIdent: String, event: DomainEvent, serializedEvent: Array[Byte]): BinaryDomainEventLogRow =
    BinaryDomainEventLogRow(event.header.id, event.header.aggRef.id, event.header.aggRef.version, event.header.timestamp, channel, typeIdent, serializedEvent)
  override def unpackRow(row: BinaryDomainEventLogRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.eventtype)
}