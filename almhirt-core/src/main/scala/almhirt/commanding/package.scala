package almhirt

import almhirt.common._
import almhirt.core._
import almhirt.domain._
import almhirt.common.AlmFuture
import almhirt.environment.Almhirt

package object commanding {
  type CreatorCommandHandlerFuture[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: BoundDomainCommand] = (TCom, Almhirt) => AlmFuture[(AR, List[TEvent])]
  type MutatorCommandHandlerFuture[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: BoundDomainCommand] = (TCom, AR, Almhirt) => AlmFuture[(AR, List[TEvent])]
  type CreatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: BoundDomainCommand] = (TCom, Almhirt) => AlmValidation[(AR, List[TEvent])]
  type MutatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: BoundDomainCommand] = (TCom, AR, Almhirt) => AlmValidation[(AR, List[TEvent])]
}