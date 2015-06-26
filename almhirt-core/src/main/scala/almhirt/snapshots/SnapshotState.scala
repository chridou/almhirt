package almhirt.snapshots

import almhirt.aggregates._

sealed trait SnapshotState { def version: AggregateRootVersion }

object SnapshotState {
  case object SnapshotVacat extends SnapshotState { val version = AggregateRootVersion(0L) }
  final case class SnapshotVivus(version: AggregateRootVersion) extends SnapshotState
  final case class SnapshotMortuus(version: AggregateRootVersion) extends SnapshotState

  def snapshotStatefromLifecycle(lf: AggregateRootLifecycle[_]): SnapshotState = {
    lf match {
      case Vacat      ⇒ SnapshotVacat
      case Vivus(ar)  ⇒ SnapshotVivus(ar.version)
      case Mortuus(_, v) ⇒ SnapshotState.SnapshotMortuus(v)
    }
  }
  
  implicit class AggregateRootLifecycleSnapshotStateOps(val self: AggregateRootLifecycle[_]) extends AnyVal {
    def toSnapshotState: SnapshotState = SnapshotState.snapshotStatefromLifecycle(self)
  }
}