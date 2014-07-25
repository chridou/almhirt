package almhirt.core.types

import almhirt.common._

/** Used to indicate that an event couldn't be handled by an aggregate root's handler. 
 * This is a major problem as it indicates a software defect.
 */
class UnhandledDomainEventException(arId: AggregateRootId, event: DomainEvent, msg: String) extends RuntimeException(msg) {
  def this(arId: AggregateRootId, event: DomainEvent) =
    this(arId, event, s"""Aggregate root with id "${arId.toString()}" could not handle domain event of type "${event.getClass().getName()}".""")
}
