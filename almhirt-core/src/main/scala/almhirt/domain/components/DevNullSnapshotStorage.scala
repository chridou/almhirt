package almhirt.domain.components

import akka.actor._
import almhirt.common._
import almhirt.core._

class DevNullSnapshotStorage extends Actor {
  override def receive: Receive = {
    case ev: SnapshotStorageReq =>
      ev match {
        case GetSnapshotQry(id) => sender ! SnapshotRsp(None, id)
        case PutSnapshotCmd(ar) => ()
        case ContainsSnapshotQry(id) => sender ! ContainsSnapshotRsp(false, id)
        case GetVersionForSnapshot(id) => sender ! VersionForSnapshotRsp(None, id)
      }
  }
}