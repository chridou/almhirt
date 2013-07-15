package almhirt.domain.impl

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.core.Almhirt
import almhirt.domain.AggregateRootCell
import almhirt.domain._
import almhirt.domain.DomainEvent

trait SimpleAggregateRootCell extends AggregateRootCell with AggregateRootCellWithEventValidation { self: Actor =>
  import AggregateRootCell._
  import almhirt.domaineventlog.DomainEventLog._

  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  def managedAggregateRooId: JUUID
  def rebuildAggregateRoot(events: Iterable[Event]): DomainValidation[AR]
  def maxAge: org.joda.time.Duration

  protected def theAlmhirt: Almhirt
  protected def domainEventLog: ActorRef

  protected def waitingState(ar: AR, activeSince: DateTime): Receive = {
    case GetAggregateRoot =>
      sender ! RequestedAggregateRoot(ar)
    case UpdateAggregateRoot(targetState, events) =>
      tryGetPotentialUpdate(Some(ar), targetState.asInstanceOf[AR], events.map(_.asInstanceOf[Event]), sender).foreach {
        case (nextState, events, waitsForUpdateResponse) =>
          context.become(updatingState(Some(ar), waitsForUpdateResponse, nextState, Vector.empty))
      }
    case CheckCachedAggregateRootAge(maxAge) =>
      if (activeSince.plus(maxAge).compareTo(theAlmhirt.getDateTime) < 0)
        context.become(passiveState())
  }

  protected def updatingState(
    currentState: Option[AR],
    requestedUpdate: ActorRef,
    potentialNextState: AR,
    pendingUpdates: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case GetAggregateRoot =>
      currentState match {
        case Some(ar) => sender ! RequestedAggregateRoot(ar)
        case None => sender ! DomainMessages.AggregateRootNotFound(managedAggregateRooId)
      }
    case uar: UpdateAggregateRoot =>
      context.become(updatingState(currentState, requestedUpdate, potentialNextState, pendingUpdates :+ (sender, uar)))
    case commitResponse: CommittedDomainEvents =>
      commitResponse match {
        case NothingCommitted() =>
          requestedUpdate ! AggregateRootUpdated(potentialNextState)
          getNextUpdateTask(Some(potentialNextState), pendingUpdates) match {
            case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
              domainEventLog ! LogDomainEvents(nextUpdateEvents)
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              context.become(waitingState(potentialNextState, theAlmhirt.getDateTime))
          }
        case AllDomainEventsSuccessfullyCommitted(_) =>
          requestedUpdate ! AggregateRootUpdated(potentialNextState)
          getNextUpdateTask(Some(potentialNextState), pendingUpdates) match {
            case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
              domainEventLog ! LogDomainEvents(nextUpdateEvents)
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              context.become(waitingState(potentialNextState, theAlmhirt.getDateTime))
          }
        case DomainEventsPartiallyCommitted(committedEvents, problem, uncommittedEvents) =>
          val lastRecoverableStateV =
            currentState match {
              case Some(ar) => ar.applyEvents(committedEvents.map(_.asInstanceOf[Event]))
              case None => rebuildAggregateRoot(committedEvents.map(_.asInstanceOf[Event]))
            }
          lastRecoverableStateV.fold(
            fail => {
              requestedUpdate ! UpdateCancelled(None, problem)
              pendingUpdates.foreach(x => x._1 ! UpdateCancelled(None, problem))
              throw new PotentiallyInvalidStatePersistedException(managedAggregateRooId, problem)
            },
            lastRecoverableState => {
              requestedUpdate ! AggregateRootPartiallyUpdated(lastRecoverableState, uncommittedEvents, problem)
              pendingUpdates.foreach(x => x._1 ! UpdateCancelled(Some(lastRecoverableState), problem))
              problem.escalate
            })
        case CommitDomainEventsFailed(problem, _) =>
          requestedUpdate ! UpdateAggregateRootFailed(problem)
          getNextUpdateTask(Some(potentialNextState), pendingUpdates) match {
            case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
              domainEventLog ! LogDomainEvents(nextUpdateEvents)
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              context.become(waitingState(potentialNextState, theAlmhirt.getDateTime))
          }
      }
    case CheckCachedAggregateRootAge =>
      ()
  }

  protected def fetchArState(pendingGets: Vector[(ActorRef)], pendingUpdates: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case GetAggregateRoot =>
      context.become(fetchArState(pendingGets :+ sender, pendingUpdates))
    case upd: UpdateAggregateRoot =>
      context.become(fetchArState(pendingGets, pendingUpdates :+ (sender, upd)))
    case DomainEventsChunk(_, _, events) =>
      if (events.isEmpty) {
        pendingGets.foreach(_ ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
        getNextUpdateTask(None, pendingUpdates) match {
          case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
            domainEventLog ! LogDomainEvents(nextUpdateEvents)
            context.become(updatingState(None, requestedNextUpdate, nextUpdateState, rest))
          case NoUpdateTasks =>
            throw new NewAggregateRootWasRequiredException(managedAggregateRooId)
        }
      } else
        rebuildAggregateRoot(events.map(_.asInstanceOf[Event])).fold(
          fail => {
            pendingGets.foreach(_ ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
            pendingUpdates.foreach(_._1 ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
            throw new CouldNotRebuildAggregateRootException(managedAggregateRooId, fail)
          },
          ar => {
            pendingGets.foreach(_ ! RequestedAggregateRoot(ar))
            getNextUpdateTask(Some(ar), pendingUpdates) match {
              case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
                domainEventLog ! LogDomainEvents(nextUpdateEvents)
                context.become(updatingState(Some(ar), requestedNextUpdate, nextUpdateState, rest))
              case NoUpdateTasks =>
                context.become(waitingState(ar, theAlmhirt.getDateTime))
            }
          })
    case DomainEventsChunkFailure(_, problem) =>
      pendingGets.foreach(_ ! DomainMessages.AggregateRootFetchError(problem))
      pendingUpdates.foreach(_._1 ! DomainMessages.AggregateRootFetchError(problem))
      problem.escalate
    case CheckCachedAggregateRootAge =>
      ()
  }

  protected def passiveState(): Receive = {
    case GetAggregateRoot =>
      domainEventLog ! GetDomainEvents(managedAggregateRooId)
      context.become(fetchArState(Vector(sender), Vector.empty))
    case uar: UpdateAggregateRoot =>
      context.become(fetchArState(Vector.empty, Vector((sender, uar))))
    case CheckCachedAggregateRootAge =>
      ()
  }

  protected def receiveAggregateRootCellMsg = passiveState()
}
  
  