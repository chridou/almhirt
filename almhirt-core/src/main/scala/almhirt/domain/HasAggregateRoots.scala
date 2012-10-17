package almhirt.domain

import almhirt._

trait HasAggregateRoots[AR <: AggregateRoot[_,_], Event <: DomainEvent] {
  def get(id: java.util.UUID): AlmFuture[AR]
}