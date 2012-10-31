package almhirt

import almhirt.environment._

package object commanding {
  type MutatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = (TCom, AR) => AlmFuture[(AR, List[TEvent])]
  type CreatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = TCom => AlmFuture[(AR, List[TEvent])]
}