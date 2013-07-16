package almhirt.domain

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.common._

object AggregateRootCell {
  sealed trait AggregateRootCellMessage

  case object GetManagedAggregateRoot extends AggregateRootCellMessage

  sealed trait CachedAggregateRootControl extends AggregateRootCellMessage
  final case class ClearCachedOlderThan(maxAge: org.joda.time.Duration) extends CachedAggregateRootControl
  case object ClearCached extends CachedAggregateRootControl

  final case class AggregateRootWasDeleted(arId: java.util.UUID) extends AggregateRootCellMessage

  final case class AggregateRootPartiallyUpdated(newState: IsAggregateRoot, uncommittedEvents: Iterable[DomainEvent], problem: Problem) extends AggregateRootCellMessage
  final case class UpdateCancelled(lastKnownState: Option[IsAggregateRoot], problem: almhirt.common.Problem) extends AggregateRootCellMessage
}

trait AggregateRootCell { self: Actor =>
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  protected def receiveAggregateRootCellMsg: Receive
}

trait AggregateRootCellWithEventValidation { self: AggregateRootCell =>
  import scalaz.syntax.validation._
  import almhirt.almvalidation.kit._
  import AggregateRootCell._
  import DomainMessages._

  def managedAggregateRooId: JUUID

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
          requestsUpdate ! AggregateRootUpdateFailed(fail)
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
        requestsUpdate ! AggregateRootUpdateFailed(fail)
        None
      },
      arAndEvents =>
        Some(arAndEvents._1, arAndEvents._2, requestsUpdate))
  }

  def validateAggregateRootsAgainstEvents(currentState: Option[AR], newState: AR, uncommittedEvents: IndexedSeq[Event]): AlmValidation[(AR, IndexedSeq[Event])] =
    if (uncommittedEvents.isEmpty)
      EmptyCollectionProblem(s"""No events to append for "${newState.id.toString()}"""").failure
    else if (newState.id != managedAggregateRooId)
      UnspecifiedProblem(s"""The potential new state has the id "${newState.id.toString()}" but the aggregate root managed by this cell has the id "${managedAggregateRooId.toString()}".""").failure
    else if (uncommittedEvents.exists(_.aggId != managedAggregateRooId))
      UnspecifiedProblem(s"""At least one domain event to be committed has an aggregate root id different from aggregate root managed by this cell: "${managedAggregateRooId.toString()}".""").failure
    else if (uncommittedEvents.size > 1 && !uncommittedEvents.sliding(2).forall(elems => elems.tail.head.aggVersion - elems.head.aggVersion == 1))
      UnspecifiedProblem(s"""The events to be committed for "${managedAggregateRooId.toString()}" do not have consecutive versions.""").failure
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
    else if (uncommittedEvents.head.aggVersion != 0L)
      UnspecifiedProblem(s"""When creating a new aggregate root("${newState.id.toString()}") the first event must have a version of 0. It is ${uncommittedEvents.head.aggVersion}.""").failure
    else
      (newState, uncommittedEvents).success

  private def validateforMutatedAggregateRoot(currentState: AR, newState: AR, uncommittedEvents: IndexedSeq[Event]): AlmValidation[(AR, IndexedSeq[Event])] =
    if (newState.id != currentState.id)
      UnspecifiedProblem(s"""When mutating an aggregate root("${newState.id.toString()}") it must have the same id as the current state("${currentState.id.toString()}).""").failure
    else if (uncommittedEvents.exists(_.isInstanceOf[CreatesNewAggregateRootEvent]))
      UnspecifiedProblem(s"""When mutating an aggregate root("${newState.id.toString()}") no event may inherit CreatesNewAggregateRootEvent.\nDid you try to recreate an existing aggregate root?""").failure
    else if (currentState.version != uncommittedEvents.head.aggVersion)
      UnspecifiedProblem(s"""When mutating an aggregate root("${newState.id.toString()}") the first events version(${uncommittedEvents.head.aggVersion}) must equal the current aggregate roots version(${currentState.version}).""").failure
    else if (newState.version != currentState.version + uncommittedEvents.length)
      UnspecifiedProblem(s"""When mutating an aggregate root("${newState.id.toString()}") the new version(${newState.version.toString()}) must equal the current aggregate roots version(${currentState.version.toString()}) plus the number of events(${uncommittedEvents.length}) which is ${(currentState.version + uncommittedEvents.length).toString}.""").failure
    else
      (newState, uncommittedEvents).success

}