package almhirt.eventsourcing

import scalaz.{NonEmptyList, Validation}
import almhirt.validation.Problem
import almhirt.concurrent.AlmFuture
import almhirt.domain.EntityEvent

class CommittedEntityEvents(val events: NonEmptyList[EntityEvent])

trait CanLogEntityEvents {
  // takes in reversed order, returns in correct order
  def logEvents(events: NonEmptyList[EntityEvent]): AlmFuture[CommittedEntityEvents]
}
