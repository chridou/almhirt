package almhirt.domain.components

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.IsAggregateRoot

sealed trait SnapshotStorageMessage
sealed trait SnapshotStorageReq extends SnapshotStorageMessage
final case class GetSnapshotQry(id: JUUID) extends SnapshotStorageReq
final case class PutSnapshotCmd(ar: IsAggregateRoot) extends SnapshotStorageReq
final case class ContainsSnapshotQry(id: JUUID) extends SnapshotStorageReq
final case class GetVersionForSnapshot(id: JUUID) extends SnapshotStorageReq

sealed trait SnapshotStorageRsp extends SnapshotStorageMessage
final case class SnapshotRsp(snapshot: Option[IsAggregateRoot], queriedId: JUUID) extends SnapshotStorageMessage
final case class ContainsSnapshotRsp(isContained: Boolean, queriedId: JUUID) extends SnapshotStorageMessage
final case class VersionForSnapshotRsp(version: Option[Long], queriedId: JUUID) extends SnapshotStorageMessage
