package almhirt.domain

import scalaz.syntax.validation._
import scalaz.std._
import almhirt._

trait CanValidateAggregateRootsAgainstEvents[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  def validateAggregateRootAgainstEvents(newAr: AR, uncommittedEvents: List[Event], nextRequiredEventVersion: Long): AlmValidation[(AR, List[Event])] = {
    if (uncommittedEvents.isEmpty)
      EmptyCollectionProblem("no events to append", category = ApplicationProblem, severity = Minor).failure
    else if (uncommittedEvents.head.version != nextRequiredEventVersion)
      UnspecifiedProblem("The first event's version must be equal to the next required event version: %d != %d".format(uncommittedEvents.head.version, nextRequiredEventVersion), category = ApplicationProblem, severity = Minor).failure
    else if (uncommittedEvents.last.version + 1L != newAr.version)
      UnspecifiedProblem("The last event's version must be one less that the aggregate root's version: %d + 1 != %d".format(uncommittedEvents.last.version, newAr.version), category = ApplicationProblem, severity = Minor).failure
    else {
      uncommittedEvents match {
        case List(x) =>
          (newAr, uncommittedEvents).success
        case xs =>
          boolean.fold(
            uncommittedEvents.sliding(2).forall(elems => elems.tail.head.version - elems.head.version == 1),
            (newAr, uncommittedEvents).success,
            UnspecifiedProblem("The events do not have a consecutive version difference of 1.", category = ApplicationProblem, severity = Minor).failure)
      }
    }
  }
}