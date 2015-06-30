package almhirt.snapshots

import org.scalatest._
import almhirt.aggregates._

class SnapshottingPolicySpecs() extends FlatSpec with Matchers {
  "AlwaysSnapshoot" should
    "return Some(StoreSnapshot) for SnapShotState.Vacat and Vivus(version=1)" in {
      val policy = SnapshottingPolicy.AlwaysSnapshoot
      val agg = AggregateRoot.fromUntyped("", 1L)
      
      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return Some(StoreSnapshot) for SnapShotState.Vivus(version=1) and Vivus(version=2)" in {
      val policy = SnapshottingPolicy.AlwaysSnapshoot
      val agg = AggregateRoot.fromUntyped("", 2L)
      
      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
}