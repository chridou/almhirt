package almhirt.domain.components

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.IsAggregateRoot

trait SnapshotStorage {
  def get(id: JUUID): AlmFuture[IsAggregateRoot]
  def getSync(id: JUUID): AlmValidation[IsAggregateRoot]
  def put(ar: IsAggregateRoot): Unit
  def remove(id: JUUID): Unit
  def contains(id: JUUID): AlmFuture[Boolean]
  def containsSync(id: JUUID): AlmValidation[Boolean]
  def versionFor(id: JUUID): AlmFuture[Long]
  def versionForSync(id: JUUID): AlmValidation[Long]
}