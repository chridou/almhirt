package almhirt.domain.components

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.domain.IsAggregateRoot

trait InMemorySnapshotStorage extends Actor {
  import scala.collection.mutable._
  val snapshots: Map[JUUID, IsAggregateRoot] = Map.empty
  override def receive: Receive = {
    case ev: SnapshotStorageReq =>
      ev match {
        case GetSnapshotQry(id) => sender ! SnapshotRsp(snapshots.get(id), id)
        case PutSnapshotCmd(ar) => snapshots.put(ar.id, ar)
        case ContainsSnapshotQry(id) => sender ! ContainsSnapshotRsp(snapshots.contains(id), id)
        case GetVersionForSnapshot(id) => sender ! VersionForSnapshotRsp(snapshots.get(id).map(_.version), id)
      }
  }
}