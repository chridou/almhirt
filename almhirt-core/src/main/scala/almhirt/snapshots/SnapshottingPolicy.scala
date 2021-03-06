package almhirt.snapshots

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.aggregates._

trait SnapshottingPolicy extends Function2[AggregateRootLifecycle[_ <: AggregateRoot], SnapshotState, Option[SnapshotRepository.SnapshottingAction]] {
  final def apply(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
    requiredActionFor(newState, lastSnapshotState)

  def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction]

  final def requiredActionForAggregateRoot[T <: AggregateRoot](newState: AggregateRootLifecycle[T], oldState: AggregateRootLifecycle[T]): Option[SnapshotRepository.SnapshottingAction] =
    requiredActionFor(newState, SnapshotState.snapshotStateFromLifecycle(oldState))
}

object SnapshottingPolicy {
  def apply(atLeastEveryN: Int, startAt: AggregateRootVersion): SnapshottingPolicy = {
    val AggregateRootVersion(v) = startAt
    if (atLeastEveryN <= 0) {
      NeverSnapshoot
    } else if (atLeastEveryN == 1) {
      if (startAt > AggregateRootVersion(0L)) {
        snapshootAlwaysStartAt(startAt)
      } else {
        AlwaysSnapshoot
      }
    } else {
      if (startAt > AggregateRootVersion(0L)) {
        snapshootAtLeastEveryNStartAtVersion(atLeastEveryN, startAt)
      } else {
        snapshootAtLeastEveryN(atLeastEveryN)
      }
    }
  }

  object NeverSnapshoot extends SnapshottingPolicy {
    def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
      None
  }

  object AlwaysSnapshoot extends SnapshottingPolicy {
    def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
      (lastSnapshotState, newState) match {
        case (SnapshotState.SnapshotVacat, Vivus(ar)) ⇒
          Some(SnapshotRepository.StoreSnapshot(ar))
        case (SnapshotState.SnapshotVivus(snapshotVersion), Vivus(newAr)) if (newAr.version > snapshotVersion) ⇒
          Some(SnapshotRepository.StoreSnapshot(newAr))
        case (SnapshotState.SnapshotVivus(snapshotVersion), Mortuus(id, version)) ⇒
          Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
        case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
          Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
        case _ ⇒
          None
      }
  }

  def snapshootAlwaysStartAt(startAt: AggregateRootVersion): SnapshottingPolicy =
    new SnapshottingPolicy {
      def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
        (lastSnapshotState, newState) match {
          case (SnapshotState.SnapshotVacat, Vivus(ar)) ⇒
            if (ar.version >= startAt)
              Some(SnapshotRepository.StoreSnapshot(ar))
            else None
          case (SnapshotState.SnapshotVivus(snapshotVersion), Vivus(newAr)) if (newAr.version > snapshotVersion) ⇒
            if (newAr.version >= startAt)
              Some(SnapshotRepository.StoreSnapshot(newAr))
            else None
          case (SnapshotState.SnapshotVivus(snapshotVersion), Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
          case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
          case _ ⇒
            None
        }
    }

  def snapshootAtLeastEveryN(n: Int): SnapshottingPolicy =
    new SnapshottingPolicy {
      def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
        (lastSnapshotState, newState) match {
          case (SnapshotState.SnapshotVacat, Vivus(ar)) ⇒
            if (ar.version - SnapshotState.SnapshotVacat.version >= n)
              Some(SnapshotRepository.StoreSnapshot(ar))
            else None
          case (SnapshotState.SnapshotVivus(snapshotVersion), Vivus(newAr)) ⇒
            if (newAr.version - snapshotVersion >= n)
              Some(SnapshotRepository.StoreSnapshot(newAr))
            else None
          case (SnapshotState.SnapshotVivus(snapshotVersion), Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
          case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
          case _ ⇒
            None
        }
    }

  def snapshootAtLeastEveryNStartAtVersion(n: Int, startAt: AggregateRootVersion): SnapshottingPolicy =
    new SnapshottingPolicy {
      def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
        (lastSnapshotState, newState) match {
          case (SnapshotState.SnapshotVacat, Vivus(ar)) ⇒
            if (ar.version >= startAt && ar.version - SnapshotState.SnapshotVacat.version >= n)
              Some(SnapshotRepository.StoreSnapshot(ar))
            else None
          case (SnapshotState.SnapshotVivus(snapshotVersion), Vivus(newAr)) ⇒
            if (newAr.version >= startAt && newAr.version - snapshotVersion >= n)
              Some(SnapshotRepository.StoreSnapshot(newAr))
            else None
          case (SnapshotState.SnapshotVivus(snapshotVersion), Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
          case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootMortuus(id, version))
          case _ ⇒
            None
        }
    }

  import almhirt.configuration._
  import com.typesafe.config.Config
  implicit object SnapshottingPolicyConfigExtractor extends ConfigExtractor[SnapshottingPolicy] {
    def getValue(config: Config, path: String): AlmValidation[SnapshottingPolicy] =
      for {
        section ← config.v[Config](path)
        storeEvery ← section.magicDefault[Int]("never", 0)("every")
        startAt ← section.magicOption[Long]("start-at")
      } yield {
        val sa = AggregateRootVersion(startAt getOrElse 0L)
        SnapshottingPolicy(storeEvery, sa)
      }

    def tryGetValue(config: Config, path: String): AlmValidation[Option[SnapshottingPolicy]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

}