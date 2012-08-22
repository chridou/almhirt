package almhirt.domain

/* Implementors are supposed to be able to handle domain events
 * 
 * @tparam AR The type of the aggregate root this trait is mixed in
 * @tparam Event The base type of [[almhirt.domain.DomainEvent]]s handled by this trait
 */
trait CanHandleDomainEvent[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  /** A function that applies a single [[almhirt.domain.DomainEvent]] and returns the effect */
  def applyEvent: Event => DomainValidation[AR]
}
