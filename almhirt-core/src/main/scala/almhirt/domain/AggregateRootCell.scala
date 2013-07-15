package almhirt.domain

import akka.actor._
import almhirt.common._

object AggregateRootCell {
  final case class UpdateAggregateRoot(ar: IsAggregateRoot, events: IndexedSeq[DomainEvent])
  case object GetAggregateRoot
  final case class CheckCachedAggregateRootAge(maxAge: org.joda.time.Duration)

  final case class RequestedAggregateRoot(ar: IsAggregateRoot)
  final case class AggregateRootUpdated(newState: IsAggregateRoot)
  final case class AggregateRootPartiallyUpdated(newState: IsAggregateRoot, uncommittedEvents: Iterable[DomainEvent], problem: Problem)
  final case class UpdateAggregateRootFailed(problem: almhirt.common.Problem)
  final case class UpdateCancelled(lastKnownState: Option[IsAggregateRoot], problem: almhirt.common.Problem)
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

  def getNextUpdateTask(currentState: Option[AR], requestedUpdates: Vector[(ActorRef, UpdateAggregateRoot)]): UpdateTask =
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

  def validateAggregateRootsAgainstEvents(currentState: Option[AR], newState: AR, uncommittedEvents: IndexedSeq[Event]): AlmValidation[(AR, IndexedSeq[Event])] =
    if (uncommittedEvents.isEmpty)
      EmptyCollectionProblem(s"""No events to append for "${newState.id.toString()}"""").failure
    else
      currentState match {
        case Some(toMutate) =>
          validateforMutatedAggregateRoot(toMutate, newState, uncommittedEvents)
        case None =>
          validateForNewAggregateRoot(newState, uncommittedEvents)
      }

  private def validateForNewAggregateRoot(newState: AR, uncommittedEvents: IndexedSeq[Event]): AlmValidation[(AR, IndexedSeq[Event])] =
    if (!uncommittedEvents.head.isInstanceOf[CreatesNewAggregateRootEvent])
      UnspecifiedProblem(s"""When creating a new aggregate root("${newState.id.toString()}") the first event must inherit CreatesNewAggregateRootEvent.""").failure
    else if (newState.version != uncommittedEvents.length)
      UnspecifiedProblem(s"""When creating a new aggregate root("${newState.id.toString()}") the number of events(${uncommittedEvents.length.toString()}) must equal the new aggregate roots version(${newState.version.toString()}).""").failure
    else
      (newState, uncommittedEvents).success

  private def validateforMutatedAggregateRoot(currentState: AR, newState: AR, uncommittedEvents: IndexedSeq[Event]): AlmValidation[(AR, IndexedSeq[Event])] =
    if (newState.id != currentState.id)
      UnspecifiedProblem(s"""When mutating an aggregate root("${newState.id.toString()}") it must have the same id as the current state("${currentState.id.toString()}).""").failure
    else if (uncommittedEvents.exists(_.isInstanceOf[CreatesNewAggregateRootEvent]))
      UnspecifiedProblem(s"""When mutating an aggregate root("${newState.id.toString()}") no event may inherit CreatesNewAggregateRootEvent.""").failure
    else if (newState.version != currentState.version + uncommittedEvents.length)
      UnspecifiedProblem(s"""When creating an aggregate root("${newState.id.toString()}") new version(${newState.version.toString()}) must equal the current aggregate roots version(${currentState.version.toString()}) plus the number of events(${uncommittedEvents.length}) which is ${(currentState.version + uncommittedEvents.length).toString}.""").failure
    else
      (newState, uncommittedEvents).success

}