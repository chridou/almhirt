package almhirt.domain

import almhirt.common._

/** Used to indicate that an event couldn't be handled by an aggregate root's handler. 
 * This is a major problem as it indicates a software defect.
 */
class UnhandledDomainEventException(arId: java.util.UUID, event: DomainEvent) extends RuntimeException(s"""Aggregate root with id "${arId.toString()}" could not handle domain event of type "${event.getClass().getName()}".""")

class PotentiallyInvalidStatePersistedException(arId: java.util.UUID, problem: Problem) extends RuntimeException(s"""Aggregate root with id "${arId.toString()}" could be in an invalid state!""")

class NewAggregateRootWasRequiredException(arId: java.util.UUID) extends RuntimeException(s"""Aggregate root with id "${arId.toString()}" does not exist nor one was created""")

class CouldNotRebuildAggregateRootException(arId: java.util.UUID, problem: Problem) extends RuntimeException(s"""Aggregate root with id "${arId.toString()}" could not be rebuild""")
