package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import almhirt.domain.components.SyncSnapshotStorage
import almhirt.domain.IsAggregateRoot
import almhirt.ext.core.slick.shared.BlobStoreComponent

abstract class SyncSlickSnapshotStorageBase[TRow <: SnapshotRow](
  dal: SnapshotStorageComponent[TRow] with BlobStoreComponent,
  blobPolicy: BlobPolicy,
  serializing: CanSerializeToFixedChannelAndDeserialize[IsAggregateRoot, IsAggregateRoot] { type SerializedRepr = TRow#Repr }) extends SyncSnapshotStorage {

  def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  def getSnapshot(id: JUUID): AlmValidation[IsAggregateRoot] =
    for {
      serialized <- dal.getSnapshotRowById(id).map(unpackRow)
      deserialized <- serializing.deserialize(serialized._2)(serialized._1, Some(serialized._3))
    } yield deserialized

  def putSnapshot(ar: IsAggregateRoot): AlmValidation[IsAggregateRoot] =
    serializing.serialize(ar, None).flatMap {
      case (Some(ti), serializedEvent) =>
        val row = createRow(serializing.channel, ti, ar, serializedEvent)
        dal.insertSnapshotRow(row)
      case (None, _) => UnspecifiedProblem("A type identisier is required.").failure
    }.map(_ => ar)

  def containsSnapshot(id: JUUID): AlmValidation[Boolean] =
    dal.isSnapshotContained(id)

  def getVersionForSnapshot(id: JUUID): AlmValidation[Long] =
    dal.getVersionForId(id)

}

class SlickSyncTextSnapshotStorage(
  dal: SnapshotStorageComponent[TextSnapshotRow] with BlobStoreComponent,
  blobPolicy: BlobPolicy,
  serializing: StringSerializingToFixedChannel[IsAggregateRoot, IsAggregateRoot]) extends SyncSlickSnapshotStorageBase[TextSnapshotRow](dal, blobPolicy, serializing) {
  override def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedAr: String): TextSnapshotRow =
    TextSnapshotRow(ar.id, ar.version, typeIdent, channel, serializedAr)
  override def unpackRow(row: TextSnapshotRow): (String, String, String) =
    (row.payload, row.channel, row.arType)
}

class SlickSyncBinarySnapshotStorage(
  dal: SnapshotStorageComponent[BinarySnapshotRow] with BlobStoreComponent,
  blobPolicy: BlobPolicy,
  serializing: BinarySerializingToFixedChannel[IsAggregateRoot, IsAggregateRoot]) extends SyncSlickSnapshotStorageBase[BinarySnapshotRow](dal, blobPolicy, serializing) {
  override def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedAr: Array[Byte]): BinarySnapshotRow =
    BinarySnapshotRow(ar.id, ar.version, typeIdent, channel, serializedAr)
  override def unpackRow(row: BinarySnapshotRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.arType)
}
