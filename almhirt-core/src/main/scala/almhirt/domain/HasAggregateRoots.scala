package almhirt.domain

import almhirt.core.AlmFuture

trait HasAggregateRoots[AR <: AggregateRoot[AR,Event], Event <: DomainEvent] {
  def get(id: java.util.UUID): AlmFuture[AR]
}