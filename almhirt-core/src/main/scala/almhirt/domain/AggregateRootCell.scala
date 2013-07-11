package almhirt.domain

import akka.actor._
import almhirt.common._

object AggregateRootCell {
  final case class UpdateAggregateRoot(ar: IsAggregateRoot, events: IndexedSeq[DomainEvent])
  case object GetAggregateRoot
  case object CheckAggregateRootAge

  final case class RequestedAggregateRoot(ar: IsAggregateRoot)
  final case class AggregateRootUpdated(newState: IsAggregateRoot)
  final case class AggregateRootPartiallyUpdated(newState: IsAggregateRoot, uncommittedEvents: Iterable[DomainEvent], problem: Problem)
  final case class UpdateAggregateRootFailed(problem: almhirt.common.Problem)
  final case class UpdateCancelled(newState: IsAggregateRoot, problem: almhirt.common.Problem)
}

trait AggregateRootCell { self: Actor =>
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  protected def receiveAggregateRootCellMsg: Receive
}

trait AggregateRootCellWithEventValidation { self: AggregateRootCell =>
  import AggregateRootCell._
  import scalaz.syntax.validation._
  import almhirt.almvalidation.kit._

  protected sealed trait UpdateTask
  protected final case class NextUpdateTask(nextUpdateState: AR, nextUpdateEvents: IndexedSeq[Event], requestedNextUpdate: ActorRef, rest: Vector[(ActorRef, UpdateAggregateRoot)]) extends UpdateTask
  protected case object NoUpdateTasks extends UpdateTask

  def getNextUpdateTask(currentState: Option[AR], requestedUpdates: Vector[( ActorRef, UpdateAggregateRoot)]): UpdateTask =
    if (requestedUpdates.isEmpty)
      NoUpdateTasks
    else {
      val ((requestsUpdate, update), tail) = (requestedUpdates.head, requestedUpdates.tail)
      val potentialUpdateV = inTryCatch {
        tryGetPotentialUpdate(currentState, update.ar.asInstanceOf[AR], update.events.map(_.asInstanceOf[Event]), requestsUpdate)
      }
      potentialUpdateV.fold(
        fail => {
          requestsUpdate ! UpdateAggregateRootFailed(fail)
          getNextUpdateTask(currentState, tail)
        },
        potentialUpdate =>
          potentialUpdate match {
            case Some((newAr, newEvents, requestsNextUpdate)) =>
              NextUpdateTask(newAr, newEvents, requestsNextUpdate, tail)
            case None =>
              getNextUpdateTask(currentState, tail)
          })
    }

  def tryGetPotentialUpdate(currentState: Option[AR], newState: AR, events: IndexedSeq[Event], requestsUpdate: ActorRef): Option[(AR, IndexedSeq[Event], ActorRef)] = {
    validateAggregateRootsAgainstEvents(currentState, newState, events).fold(
      fail => {
        requestsUpdate ! UpdateAggregateRootFailed(fail)
        None
      },
      arAndEvents =>
        Some(arAndEvents._1, arAndEvents._2, requestsUpdate))
  }

  def validateAggregateRootsAgainstEvents(currentState: Option[AR], newAr: AR, uncommittedEvents: IndexedSeq[Event]): AlmValidation[(AR, IndexedSeq[Event])] = {
    //    if (uncommittedEvents.isEmpty)
    //      EmptyCollectionProblem("no events to append").failure
    //      
    //    
    //    else if (uncommittedEvents.head.aggVersion != ar.version)
    //      UnspecifiedProblem("The first event's version must be equal to the next required event version: %d != %d".format(uncommittedEvents.head.aggVersion, ar.version)).failure
    //    else if (uncommittedEvents.last.aggVersion + 1L != ar.version)
    //      UnspecifiedProblem("The last event's version must be one less that the aggregate root's version: %d + 1 != %d".format(uncommittedEvents.last.aggVersion, ar.version)).failure
    //    else {
    //      uncommittedEvents match {
    //        case Vector(x) =>
    //          (ar, uncommittedEvents).success
    //        case xs =>
    //          if (uncommittedEvents.sliding(2).forall(elems => elems.tail.head.aggVersion - elems.head.aggVersion == 1))
    //            (ar, uncommittedEvents).success
    //          else
    //            UnspecifiedProblem("The events do not have a consecutive version difference of 1.").failure
    //      }
    //    }
    ???
  }

}