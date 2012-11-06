package almhirt.domain

import scalaz.syntax.validation._
import scalaz.std._
import almhirt._

trait CanValidateAggregateRootsAgainstEvents[AR <: AggregateRoot[AR,Event], Event <: DomainEvent] {
  def validateAggregateRootAgainstEvents(ar: AR, uncommittedEvents: List[Event]): AlmValidation[(AR, List[Event])] = {
    if (uncommittedEvents.isEmpty) 
      EmptyCollectionProblem("no events to append", category = ApplicationProblem, severity = Minor).failure
    else if(uncommittedEvents.last.version + 1L != ar.version)
      UnspecifiedProblem("The last event's version must be one less that the aggregate root's version: %d + 1 != %d".format(uncommittedEvents.last.version, ar.version), category = ApplicationProblem, severity = Minor).failure
    else {
      uncommittedEvents match {
        case List(x) => 
          (ar, uncommittedEvents).success
        case xs => 
          boolean.fold(
              uncommittedEvents.sliding(2).forall(elems => elems.head.version - elems.tail.head.version == 1),
              (ar, uncommittedEvents).success,
              UnspecifiedProblem("The events do not have a consecutive version difference of 1.", category = ApplicationProblem, severity = Minor).failure)
      }
    }
  }
}