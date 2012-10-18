package almhirt.domain

trait AggregateRootRepository[AR <: AggregateRoot[AR,Event], Event <: DomainEvent] extends HasAggregateRoots[AR, Event] with StoresAggregateRoots[AR, Event]