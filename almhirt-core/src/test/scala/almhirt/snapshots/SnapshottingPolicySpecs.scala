package almhirt.snapshots

import org.scalatest._
import almhirt.aggregates._

class SnapshottingPolicySpecs() extends FlatSpec with Matchers {
  "AlwaysSnapshoot" should
    "return action=Some(StoreSnapshot) for SnapShotState.Vacat and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.AlwaysSnapshoot
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=Some(StoreSnapshot) for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=2)" in {
      val policy = SnapshottingPolicy.AlwaysSnapshoot
      val agg = AggregateRoot.fromUntyped("", 2L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=None for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.AlwaysSnapshoot
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vivus(version=2) and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.AlwaysSnapshoot
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(2L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vacat and aggregate root state = Vacat" in {
      val policy = SnapshottingPolicy.AlwaysSnapshoot

      policy.requiredActionFor(Vacat, SnapshotState.SnapshotVacat) should equal(None)
    }

  "snapshootAlwaysStartAt(10)" should
    "return action=None for SnapShotState.Vacat and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=2)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 2L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vivus(version=2) and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(2L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vacat and aggregate root state = Vacat" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))

      policy.requiredActionFor(Vacat, SnapshotState.SnapshotVacat) should equal(None)
    }
  it should
    "return action=Some(StoreSnapshot) for SnapShotState.Vacat and aggregate root state = Vivus(version=10)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 10L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=Some(StoreSnapshot) for SnapShotState.Vivus(version=9) and aggregate root state = Vivus(version=10)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 10L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(9L))) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=None for SnapShotState.Vivus(version=10) and aggregate root state = Vivus(version=10)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 10L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(10L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vivus(version=12) and aggregate root state = Vivus(version=11)" in {
      val policy = SnapshottingPolicy.snapshootAlwaysStartAt(AggregateRootVersion(10L))
      val agg = AggregateRoot.fromUntyped("", 11L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(12L))) should equal(None)
    }

  "SnapshootAtLeastEveryN(n = 10)" should
    "return action=Some(StoreSnapshot) for SnapShotState.Vacat and aggregate root state = Vivus(version=10)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 10L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=Some(StoreSnapshot) for SnapShotState.Vacat and aggregate root state = Vivus(version=11)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 11L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=None for SnapShotState.Vacat and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vacat and aggregate root state = Vivus(version=9)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 9L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVacat) should equal(None)
    }
  it should
    "return action=Some(StoreSnapshot) for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=11)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 11L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=Some(StoreSnapshot) for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=12)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 12L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=Some(StoreSnapshot) for SnapShotState.Vivus(version=20) and aggregate root state = Vivus(version=30)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 30L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(20L))) should equal(Some(SnapshotRepository.StoreSnapshot(agg)))
    }
  it should
    "return action=None for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=10)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 2L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vivus(version=1) and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 10L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(1L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vivus(version=2) and aggregate root state = Vivus(version=1)" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)
      val agg = AggregateRoot.fromUntyped("", 1L)

      policy.requiredActionFor(Vivus(agg), SnapshotState.SnapshotVivus(AggregateRootVersion(2L))) should equal(None)
    }
  it should
    "return action=None for SnapShotState.Vacat and aggregate root state = Vacat" in {
      val policy = SnapshottingPolicy.snapshootAtLeastEveryN(n = 10)

      policy.requiredActionFor(Vacat, SnapshotState.SnapshotVacat) should equal(None)
    }
}