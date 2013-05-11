package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import almhirt.domain.components.SyncSnapshotStorage
import almhirt.domain.IsAggregateRoot

abstract class SyncSlickSnapshotStorageBase[TRow <: SnapshotRow](
  dal: SnapshotStorageComponent[TRow],
  serializing: CanSerializeToFixedChannelAndDeserialize[IsAggregateRoot, IsAggregateRoot] { type SerializedRepr = TRow#Repr }) extends SyncSnapshotStorage {

  def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  def putSnapshot(ar: IsAggregateRoot): AlmValidation[IsAggregateRoot] =
    serializing.serialize(ar).flatMap {
      case (serializedAr, Some(ti)) =>
        val row = createRow(serializing.channel, ti, ar, serializedAr)
        dal.insertSnapshotRow(row)
      case (_, None) => UnspecifiedProblem("A type identifier is required.").failure
    }.map(_ => ar)

  def getSnapshot(id: JUUID): AlmValidation[IsAggregateRoot] =
    for {
      serialized <- dal.getSnapshotRowById(id).map(unpackRow)
      deserialized <- serializing.deserialize(serialized._2)(serialized._1)
    } yield deserialized

  def containsSnapshot(id: JUUID): AlmValidation[Boolean] =
    dal.isSnapshotContained(id)

  def getVersionForSnapshot(id: JUUID): AlmValidation[Long] =
    dal.getVersionForId(id)

}

class SlickSyncTextSnapshotStorage(
  dal: SnapshotStorageComponent[TextSnapshotRow],
  serializing: StringSerializingToFixedChannel[IsAggregateRoot, IsAggregateRoot]) extends SyncSlickSnapshotStorageBase[TextSnapshotRow](dal, serializing) {
  override def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedAr: String): TextSnapshotRow =
    TextSnapshotRow(ar.id, ar.version, typeIdent, channel, serializedAr)
  override def unpackRow(row: TextSnapshotRow): (String, String, String) =
    (row.payload, row.channel, row.arType)
}

class SlickSyncBinarySnapshotStorage(
  dal: SnapshotStorageComponent[BinarySnapshotRow],
  serializing: BinarySerializingToFixedChannel[IsAggregateRoot, IsAggregateRoot]) extends SyncSlickSnapshotStorageBase[BinarySnapshotRow](dal, serializing) {
  override def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedAr: Array[Byte]): BinarySnapshotRow =
    BinarySnapshotRow(ar.id, ar.version, typeIdent, channel, serializedAr)
  override def unpackRow(row: BinarySnapshotRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.arType)
}
