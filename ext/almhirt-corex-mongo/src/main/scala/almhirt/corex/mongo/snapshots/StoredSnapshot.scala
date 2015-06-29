package almhirt.corex.mongo.snapshots

import almhirt.aggregates._
import reactivemongo.bson.BSONDocument


private[snapshots] sealed trait StoredSnapshot {
  def aggId: AggregateRootId
  def version: AggregateRootVersion
}


private[snapshots] case class StoredBinaryVivusSnapshot(aggId: AggregateRootId, version: AggregateRootVersion, data: Array[Byte]) extends StoredSnapshot
private[snapshots] case class StoredBsonVivusSnapshot(aggId: AggregateRootId, version: AggregateRootVersion, data: BSONDocument) extends StoredSnapshot
private[snapshots] case class StoredMortuusSnapshot(aggId: AggregateRootId, version: AggregateRootVersion) extends StoredSnapshot