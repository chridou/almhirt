package almhirt.domain.impl

import java.util.{ UUID => JUUID }
import org.joda.time.LocalDateTime
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core.Almhirt
import almhirt.domain.AggregateRootCell
import almhirt.domain._
import almhirt.domain.DomainEvent
import almhirt.messaging.MessagePublisher
import almhirt.common.CanCreateUuidsAndDateTimes
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext

trait AggregateRootCellTemplate extends AggregateRootCell with AggregateRootCellWithEventValidation { actor: Actor with ActorLogging =>
  import AggregateRootCell._
  import DomainMessages._
  import almhirt.domaineventlog.DomainEventLog._

  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  implicit def execContext: ExecutionContext

  protected def domainEventLog: ActorRef

  protected def onDoesNotExist: () => Unit
  protected def doesNotExistDelay: FiniteDuration

  protected def publisher: MessagePublisher

  protected implicit def ccuad: CanCreateUuidsAndDateTimes

  def rebuildAggregateRoot(events: Iterable[Event]): DomainValidation[AR]

  protected def getArMsWarnThreshold: Long
  protected def getArTimeout: FiniteDuration
  protected def updateArMsWarnThreshold: Long
  protected def updateArTimeout: FiniteDuration

  private def uninitializedState(pendingGetRequests: Vector[ActorRef], pendingUpdateRequests: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case Initialize =>
      fetchAR().onComplete(
        fail => self ! InitializedWithFailure(fail),
        succ => self ! Initialized(succ))
    case Initialized(arOpt) =>
      arOpt match {
        case Some(ar) =>
          if (ar.isDeleted) {
            pendingGetRequests.foreach(_ ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
            pendingUpdateRequests.map(_._1).foreach(_ ! DomainMessages.AggregateRootUpdateFailed(
              managedAggregateRooId,
              UnspecifiedProblem(s"""Aggregate root "$managedAggregateRooId" has been deleted.""")))
            switchToDoesNotExists(true)
          } else {
            pendingGetRequests.foreach(_ ! DomainMessages.RequestedAggregateRoot(ar))
            if (pendingUpdateRequests.isEmpty) {
              context.become(idleState(ar, ccuad.getUtcTimestamp))
            } else {
              context.become(updateState(Some(ar), pendingUpdateRequests))
              self ! UpdateAR
            }
          }
        case None =>
          pendingGetRequests.foreach(_ ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
          if (pendingUpdateRequests.isEmpty) {
            switchToDoesNotExists(false)
          } else {
            context.become(updateState(None, pendingUpdateRequests))
            self ! UpdateAR
          }
      }
    case InitializedWithFailure(problem) =>
      pendingGetRequests.foreach(_ ! DomainMessages.AggregateRootFetchFailed(managedAggregateRooId, problem))
      pendingUpdateRequests.map(_._1).foreach(_ ! DomainMessages.AggregateRootUpdateFailed(managedAggregateRooId, problem))
      log.error(s"""Could not initialize aggregate root "$managedAggregateRooId": ${problem.message}""")
      context.become(errorState(problem))
    case GetManagedAggregateRoot =>
      context.become(uninitializedState(pendingGetRequests :+ sender, pendingUpdateRequests))
      self ! Initialize
    case uar: UpdateAggregateRoot =>
      context.become(uninitializedState(pendingGetRequests, pendingUpdateRequests :+ (sender, uar)))
      self ! Initialize
    case _: CachedAggregateRootControl =>
      ()
    case CallDoesNotExistCallBack =>
      ()
  }

  def idleState(ar: AR, idleSince: LocalDateTime): Receive = {
    case GetManagedAggregateRoot =>
      sender ! RequestedAggregateRoot(ar)
    case uar: UpdateAggregateRoot =>
      context.become(updateState(Some(ar), Vector((sender, uar))))
      self ! UpdateAR
    case cc: CachedAggregateRootControl =>
      cc match {
        case ClearCachedOlderThan(ttl) =>
          if (idleSince.plus(ttl).compareTo(ccuad.getUtcTimestamp) < 0) {
            context.become(uninitializedState(Vector.empty, Vector.empty))
          }
        case ClearCached =>
          context.become(uninitializedState(Vector.empty, Vector.empty))
      }
    case CallDoesNotExistCallBack =>
      ()
  }

  def updateState(arOpt: Option[AR], pendingUpdateRequests: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case GetManagedAggregateRoot =>
      arOpt match {
        case Some(ar) =>
          sender ! RequestedAggregateRoot(ar)
        case None =>
          sender ! AggregateRootNotFound(managedAggregateRooId)

      }
    case uar: UpdateAggregateRoot =>
      context.become(updateState(arOpt, pendingUpdateRequests :+ (sender, uar)))
    case UpdateAR =>
      updateAggregateRoot(arOpt, pendingUpdateRequests)
      context.become(updateState(arOpt, Vector.empty))
    case SuccessfulUpdate(newStateOpt, successToNotify, rest) =>
      successToNotify.foreach(n => n._1 ! AggregateRootUpdated(n._2))
      val newPendingUpdates = rest ++ pendingUpdateRequests
      if (newStateOpt.isDefined && newStateOpt.get.isDeleted) {
        newPendingUpdates.map(_._1).foreach(_ ! AggregateRootUpdateFailed(
          managedAggregateRooId,
          UnspecifiedProblem(s"""Aggregate root "$managedAggregateRooId" has been deleted.""")))
        switchToDoesNotExists(true)
      } else {
        if (newPendingUpdates.isEmpty) {
          newStateOpt match {
            case Some(newState) =>
              context.become(idleState(newState, ccuad.getUtcTimestamp))
            case None =>
              switchToDoesNotExists(false)
          }
        } else {
          context.become(updateState(newStateOpt, newPendingUpdates))
          self ! UpdateAR
        }
      }
    case FailedUpdate(problem, rest) =>
      log.error(s"""Could not update aggregate root "$managedAggregateRooId": ${problem.message}""")
      (rest ++ pendingUpdateRequests.map(_._1)).foreach(_ ! DomainMessages.AggregateRootUpdateFailed(managedAggregateRooId, problem))
      context.become(errorState(problem))
    case _: CachedAggregateRootControl =>
      ()
    case CallDoesNotExistCallBack =>
      ()
  }

  private def doesNotExistState(isDeleted: Boolean): Receive = {
    case GetManagedAggregateRoot =>
      sender ! AggregateRootNotFound(managedAggregateRooId)
    case uar: UpdateAggregateRoot =>
      if (isDeleted) {
        sender ! AggregateRootUpdateFailed(
          managedAggregateRooId,
          UnspecifiedProblem(s"""Aggregate root "$managedAggregateRooId" has been deleted."""))
      } else {
        context.become(updateState(None, Vector((sender, uar))))
        self ! UpdateAR
      }
    case CallDoesNotExistCallBack =>
      onDoesNotExist()
    case _: CachedAggregateRootControl =>
      ()
  }

  private def errorState(problem: Problem): Receive = {
    case GetManagedAggregateRoot =>
      context.become(uninitializedState(Vector(sender), Vector.empty))
      self ! Initialize
    case uar: UpdateAggregateRoot =>
      context.become(uninitializedState(Vector.empty, Vector((sender, uar))))
      self ! Initialize
    case _: CachedAggregateRootControl =>
      ()
    case CallDoesNotExistCallBack =>
      ()
  }

  def rebuildAr(events: Seq[DomainEvent]): AlmValidation[AR] = {
    rebuildAggregateRoot(events.map(_.asInstanceOf[Event]))
  }

  def fetchAR(): AlmFuture[Option[AR]] = {
    val start = System.currentTimeMillis()
    (domainEventLog ? GetAllDomainEventsFor(managedAggregateRooId))(getArTimeout).successfulAlmFuture[FetchedDomainEvents].collectV {
      case FetchedDomainEventsBatch(events) =>
        val elapsed = System.currentTimeMillis() - start
        if (elapsed > getArMsWarnThreshold)
          log.warning(s"""Fetching the events for aggregate root $managedAggregateRooId took more than $getArMsWarnThreshold[ms]($elapsed[ms]).""")
        if (events.isEmpty)
          None.success
        else
          rebuildAr(events).map(Some(_))
      case FetchedDomainEventsChunks() =>
        UnspecifiedProblem("FetchedDomainEventsChunks not supported").failure
    }.mapTimeout(tp => {
      val elapsed = System.currentTimeMillis() - start
      OperationTimedOutProblem(s"""The domain event log failed to deliver the events for "$managedAggregateRooId" within $getArTimeout[ms]($elapsed[ms])""")
    })
  }

  def updateAggregateRoot(arOpt: Option[AR], pendingUpdates: Vector[(ActorRef, UpdateAggregateRoot)]): Unit = {
    AlmFuture { getNextUpdateTask(arOpt, pendingUpdates).success }.onComplete(
      problem => {
        self ! FailedUpdate(problem, pendingUpdates.map(_._1))
      },
      task =>
        task match {
          case NoUpdateTasks =>
            self ! SuccessfulUpdate(arOpt, None, Vector.empty)
          case NextUpdateTask(nextUpdateState, nextUpdateEvents, requestedNextUpdate, rest) =>
            val start = System.currentTimeMillis()
            (domainEventLog ? CommitDomainEvents(nextUpdateEvents))(updateArTimeout).successfulAlmFuture[CommittedDomainEvents].onComplete(
              problem =>
                problem match {
                  case OperationTimedOutProblem(p) =>
                    val elapsed = System.currentTimeMillis() - start
                    val prob = OperationTimedOutProblem(s"""The domain event log failed to append the events for "$managedAggregateRooId" within $updateArTimeout[ms]($elapsed[ms])""")
                    self ! FailedUpdate(prob, requestedNextUpdate +: rest.map(_._1))
                  case prob =>
                    self ! FailedUpdate(prob, requestedNextUpdate +: rest.map(_._1))
                },
              succ => {
                val elapsed = System.currentTimeMillis() - start
                if (elapsed > updateArMsWarnThreshold)
                  log.warning(s"""Storing ${nextUpdateEvents.size} events for aggregate root $managedAggregateRooId took more than $updateArMsWarnThreshold[ms]($elapsed[ms]).""")
                succ match {
                  case NothingCommitted() =>
                    log.warning(s"""No events have been committed for $managedAggregateRooId""")
                    self ! SuccessfulUpdate(Some(nextUpdateState), None, rest)
                  case DomainEventsSuccessfullyCommitted(committedEvents) =>
                    self ! SuccessfulUpdate(Some(nextUpdateState), Some((requestedNextUpdate, nextUpdateState)), rest)
                }
              })

        })
  }

  def switchToDoesNotExists(isDeleted: Boolean) {
    context.become(doesNotExistState(false))
    if(isDeleted || doesNotExistDelay.toMillis == 0L)
    	onDoesNotExist()
    else
      context.system.scheduler.scheduleOnce(doesNotExistDelay)(self ! CallDoesNotExistCallBack)
  }

  private def logDebugMessage(currentState: String, msg: String) {
    log.debug(s"""Cell for "${managedAggregateRooId}" on state "$currentState": $msg""")

  }

  protected def initialize() {
    self ! Initialize
  }

  protected def receiveAggregateRootCellMsg = uninitializedState(Vector.empty, Vector.empty)

  private case object Initialize
  private case class Initialized(ar: Option[AR])
  private case class InitializedWithFailure(problem: Problem)
  private case object UpdateAR

  private case class SuccessfulUpdate(newState: Option[AR], successToNotify: Option[(ActorRef, AR)], rest: Vector[(ActorRef, UpdateAggregateRoot)])
  private case class FailedUpdate(problem: Problem, rest: Vector[ActorRef])

  private case object CallDoesNotExistCallBack
}

class AggregateRootCellImpl[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](
  aggregateRooId: JUUID,
  aggregateRootFactory: Iterable[TEvent] => DomainValidation[TAR],
  theDomainEventLog: ActorRef,
  notifyOnDoesNotExist: () => Unit,
  override val doesNotExistDelay: FiniteDuration,
  override val publisher: MessagePublisher,
  override val ccuad: CanCreateUuidsAndDateTimes,
  override val execContext: ExecutionContext,
  override val getArMsWarnThreshold: Long,
  override val updateArMsWarnThreshold: Long,
  override val getArTimeout: FiniteDuration,
  override val updateArTimeout: FiniteDuration) extends AggregateRootCellTemplate with Actor with ActorLogging {

  type AR = TAR
  type Event = TEvent

  override def preStart() {
    super.preStart()
    log.debug(s"""Aggregate root cell for managed aggregate root id "$managedAggregateRooId" is about to start.""")
    initialize()
  }

  val managedAggregateRooId = aggregateRooId

  def rebuildAggregateRoot(events: Iterable[Event]): DomainValidation[AR] = aggregateRootFactory(events)

  protected def domainEventLog: ActorRef = theDomainEventLog

  protected def onDoesNotExist() = notifyOnDoesNotExist

  override def receive: Receive = receiveAggregateRootCellMsg
}
  
  