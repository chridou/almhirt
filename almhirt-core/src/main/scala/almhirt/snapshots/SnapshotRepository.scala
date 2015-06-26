package almhirt.snapshots

import almhirt.common._
import almhirt.aggregates._


object SnapshotRepository {
  sealed trait SnapshottingAction
  
  final case class StoreSnapshot(snapshoot: AggregateRoot) extends SnapshottingAction
  sealed trait StoreSnapshotResponse
  final case class SnapshotStored(id: AggregateRootId) extends StoreSnapshotResponse
  final case class StoreSnapshotFailed(id: AggregateRootId, problem: Problem) extends StoreSnapshotResponse
 
  final case class MarkAggregateRootAsDeleted(id: AggregateRootId) extends SnapshottingAction
  sealed trait MarkAggregateRootAsDeletedResponse
  final case class AggregateRootMarkedAsDeleted(id: AggregateRootId) extends MarkAggregateRootAsDeletedResponse
  final case class MarkAggregateRootAsDeletedFailed(id: AggregateRootId, problem: Problem) extends MarkAggregateRootAsDeletedResponse
  
  final case class DeleteSnapshot(id: AggregateRootId)
  sealed trait DeleteSnapshotResponse
  final case class SnapshotDeleted(id: AggregateRootId) extends DeleteSnapshotResponse
  final case class DeleteSnapshotFailed(id: AggregateRootId, problem: Problem) extends DeleteSnapshotResponse
  
  final case class FindSnapshot(id: AggregateRootId)
  sealed trait FindSnapshotResponse
  final case class FoundSnapshot(snapshoot: AggregateRoot) extends FindSnapshotResponse
  final case class SnapshotNotFound(id: AggregateRootId) extends FindSnapshotResponse
  final case class AggregateRootWasDeleted(id: AggregateRootId) extends FindSnapshotResponse
  final case class FindSnapshotFailed(id: AggregateRootId, problem: Problem) extends FindSnapshotResponse
}