package almhirt.domain.components

import java.util.{UUID => JUUID}
import almhirt.common._
import almhirt.domain.IsAggregateRoot

trait AggregateRootCache {
  def get(id: JUUID): AlmFuture[IsAggregateRoot]
  def getSync(id: JUUID): AlmValidation[IsAggregateRoot]
  def put(ar: IsAggregateRoot): Unit
  def remove(id: JUUID): Unit
  def contains(id: JUUID): AlmFuture[Boolean]
  def containsSync(id: JUUID): AlmValidation[Boolean]
}