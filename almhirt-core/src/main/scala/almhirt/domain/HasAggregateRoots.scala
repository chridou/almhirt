package almhirt.domain

import almhirt.common.AlmFuture

trait HasAggregateRoots[AR <: AggregateRoot[AR,Event], Event <: DomainEvent] {
  def get(id: java.util.UUID): AlmFuture[AR]
}