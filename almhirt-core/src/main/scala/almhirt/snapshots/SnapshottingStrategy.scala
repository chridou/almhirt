package almhirt.snapshots

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.aggregates._

trait SnapshottingStrategy extends Function2[AggregateRootLifecycle[_ <: AggregateRoot], SnapshotState, Option[SnapshotRepository.SnapshottingAction]] {
  final def apply(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
    requiredActionFor(newState, lastSnapshotState)

  def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction]

  final def requiredActionForAggregateRoot[T <: AggregateRoot](newState: AggregateRootLifecycle[T], oldState: AggregateRootLifecycle[T]): Option[SnapshotRepository.SnapshottingAction] =
    requiredActionFor(newState, SnapshotState.snapshotStatefromLifecycle(oldState))
}

object SnapshottingStrategy {
  def apply(atLeastEveryN: Int, startAt: AggregateRootVersion): SnapshottingStrategy = {
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

  object NeverSnapshoot extends SnapshottingStrategy {
    def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
      None
  }

  object AlwaysSnapshoot extends SnapshottingStrategy {
    def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
      (lastSnapshotState, newState) match {
        case (SnapshotState.SnapshotVacat, Vivus(ar)) ⇒
          Some(SnapshotRepository.StoreSnapshot(ar))
        case (SnapshotState.SnapshotVivus(snapshotVersion), Vivus(newAr)) if (newAr.version > snapshotVersion) ⇒
          Some(SnapshotRepository.StoreSnapshot(newAr))
        case (SnapshotState.SnapshotVivus(snapshotVersion), Mortuus(id, version)) ⇒
          Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
        case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
          Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
        case _ ⇒
          None
      }
  }

  def snapshootAlwaysStartAt(startAt: AggregateRootVersion): SnapshottingStrategy =
    new SnapshottingStrategy {
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
            Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
          case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
          case _ ⇒
            None
        }
    }

  def snapshootAtLeastEveryN(n: Int): SnapshottingStrategy =
    new SnapshottingStrategy {
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
            Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
          case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
          case _ ⇒
            None
        }
    }

  def snapshootAtLeastEveryNStartAtVersion(n: Int, startAt: AggregateRootVersion): SnapshottingStrategy =
    new SnapshottingStrategy {
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
            Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
          case (SnapshotState.SnapshotVacat, Mortuus(id, version)) ⇒
            Some(SnapshotRepository.MarkAggregateRootAsDeleted(id))
          case _ ⇒
            None
        }
    }

  import almhirt.configuration._
  import com.typesafe.config.Config
  implicit object SnapshottingStrategyConfigExtractor extends ConfigExtractor[SnapshottingStrategy] {
    def getValue(config: Config, path: String): AlmValidation[SnapshottingStrategy] =
      for {
        section ← config.v[Config](path)
        storeEvery ← section.magicDefault[Int]("never", 0)("every")
        startAt ← section.magicOption[Long]("every")
      } yield {
        val sa = AggregateRootVersion(startAt getOrElse 0L)
        SnapshottingStrategy(storeEvery, sa)
      }

    def tryGetValue(config: Config, path: String): AlmValidation[Option[SnapshottingStrategy]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

}