package almhirt.snapshots

import almhirt.aggregates._

sealed trait SnapshotState { def version: AggregateRootVersion }

object SnapshotState {
  case object SnapshotVacat extends SnapshotState { val version = AggregateRootVersion(0L) }
  final case class SnapshotVivus(version: AggregateRootVersion) extends SnapshotState
  final case class SnapshotMortuus(version: AggregateRootVersion) extends SnapshotState

  def snapshotStateFromLifecycle(lf: AggregateRootLifecycle[_]): SnapshotState = {
    lf match {
      case Vacat      ⇒ SnapshotVacat
      case Vivus(ar)  ⇒ SnapshotVivus(ar.version)
      case Mortuus(_, v) ⇒ SnapshotState.SnapshotMortuus(v)
    }
  }
 
  def snapshotStateFromLivingAr(ar: AggregateRoot): SnapshotState = {
    SnapshotVivus(ar.version)
  }
  
  implicit class AggregateRootLifecycleSnapshotStateOps(val self: AggregateRootLifecycle[_]) extends AnyVal {
    def toSnapshotState: SnapshotState = SnapshotState.snapshotStateFromLifecycle(self)
  }
  
  implicit class AggregateRooSnapshotStateOps(val self: AggregateRoot) extends AnyVal {
    def toSnapshotState: SnapshotState = SnapshotState.snapshotStateFromLivingAr(self)
  }
  
}