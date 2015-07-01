package almhirt.corex.mongo.snapshots

import almhirt.aggregates._
import reactivemongo.bson.BSONDocument

private[snapshots] sealed trait PersistableSnapshotState {
  def aggId: AggregateRootId
  def version: AggregateRootVersion
}

private[snapshots] sealed trait BinarySnapshotState extends PersistableSnapshotState

private[snapshots] case class PersistableBinaryVivusSnapshotState(aggId: AggregateRootId, version: AggregateRootVersion, data: Array[Byte]) extends BinarySnapshotState
private[snapshots] case class PersistableSnappyCompressedVivusSnapshotState(aggId: AggregateRootId, version: AggregateRootVersion, snappyData: Array[Byte]) extends BinarySnapshotState
private[snapshots] case class PersistableBsonVivusSnapshotState(aggId: AggregateRootId, version: AggregateRootVersion, data: BSONDocument) extends PersistableSnapshotState
private[snapshots] case class PersistableMortuusSnapshotState(aggId: AggregateRootId, version: AggregateRootVersion) extends PersistableSnapshotState