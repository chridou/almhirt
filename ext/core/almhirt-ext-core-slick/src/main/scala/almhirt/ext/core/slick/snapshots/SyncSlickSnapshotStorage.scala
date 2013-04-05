package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import almhirt.domain.components.SyncSnapshotStorage
import almhirt.domain.IsAggregateRoot
import almhirt.ext.core.slick.shared.BlobStoreComponent

abstract class SyncSlickSnapshotStorageBase[TRow <: SnapshotRow](
  dal: SnapshotStorageComponent[TRow] with BlobStoreComponent with CanStoreSnapshotRowWithBlobs[TRow],
  blobPolicy: BlobPolicy,
  serializing: CanSerializeToFixedChannelAndDeserialize[IsAggregateRoot, IsAggregateRoot] { type SerializedRepr = TRow#Repr }) extends SyncSnapshotStorage {

  def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedEvent: TRow#Repr): TRow
  def unpackRow(row: TRow): (TRow#Repr, String, String)

  final protected def blobRefsToData(blobRefs: Vector[ExtractedBlobReference]): AlmValidation[Vector[(JUUID, Array[Byte])]] =
    almhirt.almvalidation.funs.inTryCatch {
      blobRefs.map { case ExtractedBlobReference(BlobRefByUuid(uuid), data) => (uuid, data) }
    }.leftMap(prob => MappingProblem("Only a BlobRefByUuid is allowed!", cause = Some(prob)))

  def putSnapshot(ar: IsAggregateRoot): AlmValidation[IsAggregateRoot] =
    serializing.serializeBlobSeparating(blobPolicy.serializationPolicy)(ar, None).flatMap {
      case (Some(ti), serializedAr, blobs) =>
        val row = createRow(serializing.channel, ti, ar, serializedAr)
        blobRefsToData(blobs).flatMap(blobData =>
          dal.storeRowAndBlobs(row, blobData))
      case (None, _, _) => UnspecifiedProblem("A type identifier is required.").failure
    }.map(_ => ar)
  
  def getSnapshot(id: JUUID): AlmValidation[IsAggregateRoot] =
    for {
      serialized <- dal.getSnapshotRowById(id).map(unpackRow)
      deserialized <- serializing.deserializeBlobIntegrating(blobPolicy.deserializationPolicy)(serialized._2)(serialized._1, Some(serialized._3))
    } yield deserialized

  def containsSnapshot(id: JUUID): AlmValidation[Boolean] =
    dal.isSnapshotContained(id)

  def getVersionForSnapshot(id: JUUID): AlmValidation[Long] =
    dal.getVersionForId(id)

}

class SlickSyncTextSnapshotStorage(
  dal: SnapshotStorageComponent[TextSnapshotRow] with BlobStoreComponent with CanStoreSnapshotRowWithBlobs[TextSnapshotRow],
  blobPolicy: BlobPolicy,
  serializing: StringSerializingToFixedChannel[IsAggregateRoot, IsAggregateRoot]) extends SyncSlickSnapshotStorageBase[TextSnapshotRow](dal, blobPolicy, serializing) {
  override def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedAr: String): TextSnapshotRow =
    TextSnapshotRow(ar.id, ar.version, typeIdent, channel, serializedAr)
  override def unpackRow(row: TextSnapshotRow): (String, String, String) =
    (row.payload, row.channel, row.arType)
}

class SlickSyncBinarySnapshotStorage(
  dal: SnapshotStorageComponent[BinarySnapshotRow] with BlobStoreComponent with CanStoreSnapshotRowWithBlobs[BinarySnapshotRow],
  blobPolicy: BlobPolicy,
  serializing: BinarySerializingToFixedChannel[IsAggregateRoot, IsAggregateRoot]) extends SyncSlickSnapshotStorageBase[BinarySnapshotRow](dal, blobPolicy, serializing) {
  override def createRow(channel: String, typeIdent: String, ar: IsAggregateRoot, serializedAr: Array[Byte]): BinarySnapshotRow =
    BinarySnapshotRow(ar.id, ar.version, typeIdent, channel, serializedAr)
  override def unpackRow(row: BinarySnapshotRow): (Array[Byte], String, String) =
    (row.payload, row.channel, row.arType)
}
