package almhirt.domain.impl

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.core.Almhirt
import almhirt.domain.AggregateRootCell
import almhirt.domain._
import almhirt.domain.DomainEvent
import almhirt.messaging.MessagePublisher
import almhirt.common.CanCreateUuidsAndDateTimes

trait AggregateRootCellImpl extends AggregateRootCell with AggregateRootCellWithEventValidation { actor: Actor =>
  import AggregateRootCell._
  import DomainMessages._
  import almhirt.domaineventlog.DomainEventLog._

  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  protected def publisher: MessagePublisher

  def rebuildAggregateRoot(events: Iterable[Event]): DomainValidation[AR]

  implicit val theAlmhirt: Almhirt
  protected def domainEventLog: ActorRef

  protected def onDoesNotExist()
  
  private def waitingWithArState(ar: AR, activeSince: DateTime): Receive = {
    case GetManagedAggregateRoot =>
      sender ! RequestedAggregateRoot(ar)
    case UpdateAggregateRoot(targetState, events) =>
      tryGetPotentialUpdate(Some(ar), targetState.asInstanceOf[AR], events.map(_.asInstanceOf[Event]), sender).foreach {
        case (nextState, events, waitsForUpdateResponse) =>
          context.become(updatingState(Some(ar), waitsForUpdateResponse, nextState, Vector.empty))
      }
    case cc: CachedAggregateRootControl =>
      cc match {
        case ClearCachedOlderThan(ttl) =>
          if (activeSince.plus(ttl).compareTo(theAlmhirt.getDateTime) < 0)
            context.become(uninitializedState())
        case ClearCached =>
          context.become(uninitializedState())
      }
  }

  private def doesNotExistState(): Receive = {
    case GetManagedAggregateRoot =>
      sender ! DomainMessages.AggregateRootNotFound(managedAggregateRooId)
    case UpdateAggregateRoot(targetState, events) =>
      tryGetPotentialUpdate(None, targetState.asInstanceOf[AR], events.map(_.asInstanceOf[Event]), sender).foreach {
        case (nextState, events, waitsForUpdateResponse) =>
          context.become(updatingState(None, waitsForUpdateResponse, nextState, Vector.empty))
      }
    case _: CachedAggregateRootControl =>
      ()

  }

  private def updatingState(
    currentState: Option[AR],
    requestedUpdate: ActorRef,
    potentialNextState: AR,
    pendingUpdates: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case GetManagedAggregateRoot =>
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
              domainEventLog ! CommitDomainEvents(nextUpdateEvents)
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              context.become(waitingWithArState(potentialNextState, theAlmhirt.getDateTime))
          }
        case AllDomainEventsSuccessfullyCommitted(committedEvents) =>
          committedEvents.foreach(publisher.publish(_))
          requestedUpdate ! AggregateRootUpdated(potentialNextState)
          getNextUpdateTask(Some(potentialNextState), pendingUpdates) match {
            case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
              domainEventLog ! CommitDomainEvents(nextUpdateEvents)
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              context.become(waitingWithArState(potentialNextState, theAlmhirt.getDateTime))
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
              committedEvents.foreach(publisher.publish(_))
              requestedUpdate ! AggregateRootPartiallyUpdated(lastRecoverableState, uncommittedEvents, problem)
              pendingUpdates.foreach(x => x._1 ! UpdateCancelled(Some(lastRecoverableState), problem))
              throw new CommitDomainEventsFailed(managedAggregateRooId, sender.path.toString(), Some(problem))
            })
        case CommitDomainEventsFailed(problem, _) =>
          requestedUpdate ! AggregateRootUpdateFailed(problem)
          throw new CommitDomainEventsFailed(managedAggregateRooId, sender.path.toString(), Some(problem))
      }
    case _: CachedAggregateRootControl =>
      ()
  }

  private def fetchArState(pendingGets: Vector[(ActorRef)], pendingUpdates: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case GetManagedAggregateRoot =>
      context.become(fetchArState(pendingGets :+ sender, pendingUpdates))
    case upd: UpdateAggregateRoot =>
      context.become(fetchArState(pendingGets, pendingUpdates :+ (sender, upd)))
    case DomainEventsChunk(_, _, events) =>
      if (events.isEmpty) {
        pendingGets.foreach(_ ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
        getNextUpdateTask(None, pendingUpdates) match {
          case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
            domainEventLog ! CommitDomainEvents(nextUpdateEvents)
            context.become(updatingState(None, requestedNextUpdate, nextUpdateState, rest))
          case NoUpdateTasks =>
            onDoesNotExist()
            context.become(doesNotExistState())
        }
      } else
        rebuildAggregateRoot(events.map(_.asInstanceOf[Event])).fold(
          fail => {
            pendingGets.foreach(_ ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
            pendingUpdates.foreach(_._1 ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
            throw new CouldNotRebuildAggregateRootException(managedAggregateRooId, fail)
          },
          ar => {
            if (!ar.isDeleted) {
              pendingGets.foreach(_ ! RequestedAggregateRoot(ar))
              getNextUpdateTask(Some(ar), pendingUpdates) match {
                case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
                  domainEventLog ! CommitDomainEvents(nextUpdateEvents)
                  context.become(updatingState(Some(ar), requestedNextUpdate, nextUpdateState, rest))
                case NoUpdateTasks =>
                  context.become(waitingWithArState(ar, theAlmhirt.getDateTime))
              }
            } else {
              pendingGets.foreach(_ ! AggregateRootWasDeleted(managedAggregateRooId))
              pendingUpdates.foreach(_._1 ! AggregateRootWasDeleted(managedAggregateRooId))
              onDoesNotExist()
              context.become(doesNotExistState())
            }
          })
    case DomainEventsChunkFailure(_, problem) =>
      pendingGets.foreach(_ ! AggregateRootFetchFailed(managedAggregateRooId, problem))
      pendingUpdates.foreach(_._1 ! AggregateRootFetchFailed(managedAggregateRooId, problem))
      throw new FetchDomainEventsFailed(managedAggregateRooId, sender.path.toString(), Some(problem))
    case _: CachedAggregateRootControl =>
      ()
  }

  private def uninitializedState(): Receive = {
    case GetManagedAggregateRoot =>
      domainEventLog ! GetAllDomainEventsFor(managedAggregateRooId)
      context.become(fetchArState(Vector(sender), Vector.empty))
    case uar: UpdateAggregateRoot =>
      domainEventLog ! GetAllDomainEventsFor(managedAggregateRooId)
      context.become(fetchArState(Vector.empty, Vector((sender, uar))))
    case _: CachedAggregateRootControl =>
      ()
  }

  protected def receiveAggregateRootCellMsg = uninitializedState()
}
  
  