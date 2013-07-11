package almhirt.domain

import scalaz.syntax.validation._
import scalaz.std._
import almhirt.core._
import almhirt.common._

//trait CanValidateAggregateRootsAgainstEvents[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
//  def validateAggregateRootAgainstEvents(newAr: AR, uncommittedEvents: IndexedSeq[Event], nextRequiredEventVersion: Long): AlmValidation[(AR, IndexedSeq[Event])] = {
//    if (uncommittedEvents.isEmpty)
//      EmptyCollectionProblem("no events to append").failure
//    else if (uncommittedEvents.head.aggVersion != nextRequiredEventVersion)
//      UnspecifiedProblem("The first event's version must be equal to the next required event version: %d != %d".format(uncommittedEvents.head.aggVersion, nextRequiredEventVersion)).failure
//    else if (uncommittedEvents.last.aggVersion + 1L != newAr.version)
//      UnspecifiedProblem("The last event's version must be one less that the aggregate root's version: %d + 1 != %d".format(uncommittedEvents.last.aggVersion, newAr.version)).failure
//    else {
//      uncommittedEvents match {
//        case Vector(x) =>
//          (newAr, uncommittedEvents).success
//        case xs =>
//          boolean.fold(
//            uncommittedEvents.sliding(2).forall(elems => elems.tail.head.aggVersion - elems.head.aggVersion == 1),
//            (newAr, uncommittedEvents).success,
//            UnspecifiedProblem("The events do not have a consecutive version difference of 1.").failure)
//      }
//    }
//  }
//}