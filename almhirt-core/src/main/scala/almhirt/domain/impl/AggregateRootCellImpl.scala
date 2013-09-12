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

trait AggregateRootCellTemplate extends AggregateRootCell with AggregateRootCellWithEventValidation { actor: Actor with ActorLogging =>
  import AggregateRootCell._
  import DomainMessages._
  import almhirt.domaineventlog.DomainEventLog._

  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  implicit val myAlmhirt: Almhirt

  def rebuildAggregateRoot(events: Iterable[Event]): DomainValidation[AR]

  protected def domainEventLog: ActorRef

  protected def onDoesNotExist: () => Unit
  
  protected def getArMsWarnThreshold: Long
  protected def updateArMsWarnThreshold: Long

  private def publisher: MessagePublisher = myAlmhirt.messageBus

  private def waitingWithArState(ar: AR, activeSince: DateTime): Receive = {
    case GetManagedAggregateRoot =>
      sender ! RequestedAggregateRoot(ar)
    case UpdateAggregateRoot(targetState, events) =>
      tryGetPotentialUpdate(Some(ar), targetState.asInstanceOf[AR], events.map(_.asInstanceOf[Event]), sender).foreach {
        case (nextState, events, waitsForUpdateResponse) =>
          domainEventLog ! CommitDomainEvents(events)
          logDebugMessage("waitingWithArState", """Transition to "updatingState" by "UpdateAggregateRoot"""")
          context.become(updatingState(Some(ar), waitsForUpdateResponse, nextState, Vector.empty))
      }
    case cc: CachedAggregateRootControl =>
      cc match {
        case ClearCachedOlderThan(ttl) =>
          if (activeSince.plus(ttl).compareTo(myAlmhirt.getDateTime) < 0) {
            logDebugMessage("waitingWithArState", """Transition to "uninitializedState" by "ClearCachedOlderThan"""")
            context.become(uninitializedState())
          }
        case ClearCached =>
          logDebugMessage("waitingWithArState", """Transition to "uninitializedState" by "ClearCached"""")
          context.become(uninitializedState())
      }
    case Terminated(deadActor) =>
      if(deadActor == domainEventLog)
        throw new Exception(s"""My domain eventlog died! My managed aggregate root id is $managedAggregateRooId""")
  }

  private def doesNotExistState(): Receive = {
    case GetManagedAggregateRoot =>
      sender ! DomainMessages.AggregateRootNotFound(managedAggregateRooId)
    case UpdateAggregateRoot(targetState, events) =>
      tryGetPotentialUpdate(None, targetState.asInstanceOf[AR], events.map(_.asInstanceOf[Event]), sender).foreach {
        case (nextState, events, waitsForUpdateResponse) =>
          domainEventLog ! CommitDomainEvents(events)
          logDebugMessage("doesNotExistState", """Transition to "updatingState" by "UpdateAggregateRoot"""")
          context.become(updatingState(None, waitsForUpdateResponse, nextState, Vector.empty))
      }
    case _: CachedAggregateRootControl =>
      ()
    case Terminated(deadActor) =>
      if(deadActor == domainEventLog)
        throw new Exception(s"""My domain eventlog died! My managed aggregate root id is $managedAggregateRooId""")

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
      logDebugMessage("updatingState", """Transition to "updatingState" by "UpdateAggregateRoot"""")
      context.become(updatingState(currentState, requestedUpdate, potentialNextState, pendingUpdates :+ (sender, uar)))
    case commitResponse: CommittedDomainEvents =>
      commitResponse match {
        case NothingCommitted() =>
          requestedUpdate ! AggregateRootUpdated(potentialNextState)
          getNextUpdateTask(Some(potentialNextState), pendingUpdates) match {
            case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
              domainEventLog ! CommitDomainEvents(nextUpdateEvents)
              logDebugMessage("updatingState", """Transition to "updatingState" by "NothingCommitted"""")
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              logDebugMessage("updatingState", """Transition to "waitingWithArState" by "NothingCommitted"""")
              context.become(waitingWithArState(potentialNextState, myAlmhirt.getDateTime))
          }
        case DomainEventsSuccessfullyCommitted(committedEvents) =>
          committedEvents.foreach(publisher.publish(_))
          requestedUpdate ! AggregateRootUpdated(potentialNextState)
          getNextUpdateTask(Some(potentialNextState), pendingUpdates) match {
            case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
              domainEventLog ! CommitDomainEvents(nextUpdateEvents)
              logDebugMessage("updatingState", """Transition to "updatingState" by "AllDomainEventsSuccessfullyCommitted"""")
              context.become(updatingState(Some(potentialNextState), requestedNextUpdate, nextUpdateState, rest))
            case NoUpdateTasks =>
              logDebugMessage("updatingState", """Transition to "waitingWithArState" by "AllDomainEventsSuccessfullyCommitted"""")
              context.become(waitingWithArState(potentialNextState, myAlmhirt.getDateTime))
          }
      }
    case _: CachedAggregateRootControl =>
      ()
    case Terminated(deadActor) =>
      if(deadActor == domainEventLog)
        throw new Exception(s"""My domain eventlog died! My managed aggregate root id is $managedAggregateRooId""")
      
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
            logDebugMessage("fetchArState", """Transition to "updatingState" by "DomainEventsChunk"""")
            context.become(updatingState(None, requestedNextUpdate, nextUpdateState, rest))
          case NoUpdateTasks =>
            onDoesNotExist()
            logDebugMessage("fetchArState", """Transition to "doesNotExistState" by "DomainEventsChunk"""")
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
                  logDebugMessage("fetchArState", """Transition to "updatingState" by "DomainEventsChunk"""")
                  context.become(updatingState(Some(ar), requestedNextUpdate, nextUpdateState, rest))
                case NoUpdateTasks =>
                  logDebugMessage("fetchArState", """Transition to "waitingWithArState" by "DomainEventsChunk"""")
                  context.become(waitingWithArState(ar, myAlmhirt.getDateTime))
              }
            } else {
              pendingGets.foreach(_ ! AggregateRootWasDeleted(managedAggregateRooId))
              pendingUpdates.foreach(_._1 ! AggregateRootWasDeleted(managedAggregateRooId))
              onDoesNotExist()
              logDebugMessage("fetchArState", """Transition to "doesNotExistState" by "DomainEventsChunk" because the aggregate root is deleted""")
              context.become(doesNotExistState())
            }
          })
    case DomainEventsChunkFailure(_, problem) =>
      pendingGets.foreach(_ ! AggregateRootFetchFailed(managedAggregateRooId, problem))
      pendingUpdates.foreach(_._1 ! AggregateRootFetchFailed(managedAggregateRooId, problem))
      throw new FetchDomainEventsFailed(managedAggregateRooId, sender.path.toString(), Some(problem))
    case _: CachedAggregateRootControl =>
      ()
    case Terminated(deadActor) =>
      if(deadActor == domainEventLog)
        throw new Exception(s"""My domain eventlog died! My managed aggregate root id is $managedAggregateRooId""")
  }

  private def uninitializedState(): Receive = {
    case GetManagedAggregateRoot =>
      domainEventLog ! GetAllDomainEventsFor(managedAggregateRooId)
      logDebugMessage("uninitializedState", """Transition to "fetchArState" by "GetManagedAggregateRoot"""")
      context.become(fetchArState(Vector((sender, System.currentTimeMillis())), Vector.empty))
    case uar: UpdateAggregateRoot =>
      domainEventLog ! GetAllDomainEventsFor(managedAggregateRooId)
      logDebugMessage("uninitializedState", """Transition to "fetchArState by "UpdateAggregateRoot"""")
      context.become(fetchArState(Vector.empty, Vector((sender, uar, System.currentTimeMillis()))))
    case _: CachedAggregateRootControl =>
      ()
    case Terminated(deadActor) =>
      if(deadActor == domainEventLog)
        throw new Exception(s"""My domain eventlog died! My managed aggregate root id is $managedAggregateRooId""")
  }

  private def logDebugMessage(currentState: String, msg: String) {
    log.debug(s"""Cell for "${managedAggregateRooId}" on state "$currentState": $msg""")

  }

  protected def receiveAggregateRootCellMsg = uninitializedState()
  
  override def preStart() {
    actor.context.watch(domainEventLog)
  }
}

class AggregateRootCellImpl[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](
  aggregateRooId: JUUID,
  aggregateRootFactory: Iterable[TEvent] => DomainValidation[TAR],
  theDomainEventLog: ActorRef,
  notifyOnDoesNotExist: () => Unit,
  override val getArMsWarnThreshold: Long,
  override val updateArMsWarnThreshold: Long)(implicit theAlmhirt: Almhirt) extends AggregateRootCellTemplate with Actor with ActorLogging {

  type AR = TAR
  type Event = TEvent

  
  override def preStart() {
    super.preStart()
    log.debug(s"""Aggregate root cell for managed aggregate root id "$managedAggregateRooId" is about to start.""")
  }

  val managedAggregateRooId = aggregateRooId

  override val myAlmhirt: Almhirt = theAlmhirt

  def rebuildAggregateRoot(events: Iterable[Event]): DomainValidation[AR] = aggregateRootFactory(events)

  protected def domainEventLog: ActorRef = theDomainEventLog

  protected def onDoesNotExist() = notifyOnDoesNotExist

  override def receive: Receive = receiveAggregateRootCellMsg
}
  
  