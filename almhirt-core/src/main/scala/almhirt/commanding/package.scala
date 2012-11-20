package almhirt

import akka.dispatch.ExecutionContext
import almhirt.common._
import almhirt.core._
import almhirt.domain._
import almhirt.common.AlmFuture

package object commanding {
  type MutatorCommandHandlerFuture[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = (TCom, AR, ExecutionContext) => AlmFuture[(AR, List[TEvent])]
  type CreatorCommandHandlerFuture[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = (TCom, ExecutionContext) => AlmFuture[(AR, List[TEvent])]
  type MutatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = (TCom, AR) => AlmValidation[(AR, List[TEvent])]
  type CreatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = TCom => AlmValidation[(AR, List[TEvent])]
}