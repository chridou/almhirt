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
    case UpdateAggregateRoot(ar, events) =>
      ???
    case CheckAggregateRootAge =>
      if (activeSince.plus(maxAge).compareTo(theAlmhirt.getDateTime) < 0)
        context.become(passiveState())
  }

  protected def updatingState(
    oldState: Option[AR],
    requestedUpdate: ActorRef,
    potentialNextState: AR,
    pendingUpdates: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case GetAggregateRoot =>
      oldState match {
        case Some(ar) => sender ! RequestedAggregateRoot(ar)
        case None => sender ! DomainMessages.AggregateRootNotFound(managedAggregateRooId)
      }
    case uar: UpdateAggregateRoot =>
      context.become(updatingState(oldState, requestedUpdate, potentialNextState, pendingUpdates :+ (sender, uar)))
    case CommittedDomainEvents(committed, uncommitted) =>
      uncommitted match {
        case None =>
          requestedUpdate ! AggregateRootUpdated(potentialNextState)
          getNextUpdateTask(Some(potentialNextState), pendingUpdates) match {
            case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
              domainEventLog ! LogDomainEvents(nextUpdateEvents)
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              context.become(waitingState(potentialNextState, theAlmhirt.getDateTime))
          }
        case Some((uncommittedEvents, problem)) =>
          oldState match {
            case Some(ar) =>
              ar.applyEvents(uncommittedEvents.map(_.asInstanceOf[Event])).fold(
                fail => {
	              requestedUpdate ! UpdateAggregateRootFailed(problem)
	              pendingUpdates.foreach(x => x._1 ! UpdateAggregateRootFailed(problem))
                  throw new PotentialInvalidStatePersistedException(managedAggregateRooId, fail)
                },
                newState => {
	              requestedUpdate ! AggregateRootPartiallyUpdated(newState, uncommittedEvents, problem)
	              pendingUpdates.foreach(x => x._1 ! UpdateCancelled(newState, problem))
	              problem.escalate
                }
            case None =>
              ???
          }
      }
    case CheckAggregateRootAge =>
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
            AggregateRootNotFoundProblem(managedAggregateRooId).escalate
        }
      } else
        rebuildAggregateRoot(events.map(_.asInstanceOf[Event])).fold(
          _.escalate,
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
    case CheckAggregateRootAge =>
      ()
  }

  protected def passiveState(): Receive = {
    case GetAggregateRoot =>
      domainEventLog ! GetDomainEvents(managedAggregateRooId)
      context.become(fetchArState(Vector(sender), Vector.empty))
    case uar: UpdateAggregateRoot =>
      context.become(fetchArState(Vector.empty, Vector((sender, uar))))
    case CheckAggregateRootAge =>
      ()
  }

  protected def receiveAggregateRootCellMsg = passiveState()
}
  
  