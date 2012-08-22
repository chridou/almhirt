package almhirt.eventsourcing

import scalaz.{NonEmptyList, Validation}
import almhirt.validation.Problem
import almhirt.concurrent.AlmFuture
import almhirt.domain.DomainEvent

class CommittedDomainEvents(val events: NonEmptyList[DomainEvent])

trait CanLogDomainEvents {
  def logEvents(events: NonEmptyList[DomainEvent]): AlmFuture[CommittedDomainEvents]
}
