package almhirt.domain

trait CanHandleDomainEvent[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  def applyEvent: Event => DomainValidation[AR]
}
