package almhirt

package object commanding {
  type MutatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = (TCom, AR, AlmhirtEnvironment) => AlmValidation[(AR, List[TEvent])]
  type CreatorCommandHandler[AR <: domain.AggregateRoot[AR, TEvent], TEvent <: domain.DomainEvent, TCom <: DomainCommand] = (TCom, AlmhirtEnvironment) => AlmValidation[(AR, List[TEvent])]
}