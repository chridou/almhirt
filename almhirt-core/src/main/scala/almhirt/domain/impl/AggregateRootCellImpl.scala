package almhirt.domain.impl

import java.util.{ UUID => JUUID }
import scala.concurrent._
import scala.concurrent.duration._
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
import play.api.libs.iteratee._

trait AggregateRootCellTemplate extends AggregateRootCell with AggregateRootCellWithEventValidation { actor: Actor with ActorLogging =>
  import AggregateRootCell._
  import DomainMessages._
  import almhirt.domaineventlog.DomainEventLog._

  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  implicit def execContext: ExecutionContext

  protected def domainEventLog: ActorRef

  protected def reportCellState: AggregateRootCellStateSink
  protected def cellStateReportingDelay: FiniteDuration

  protected def publisher: MessagePublisher

  protected implicit def ccuad: CanCreateUuidsAndDateTimes

  protected def createFreshAggregateRoot: Event => DomainValidation[AR]

  protected def getArWarnThreshold: FiniteDuration
  protected def getArTimeout: FiniteDuration
  protected def updateArWarnThreshold: FiniteDuration
  protected def updateArTimeout: FiniteDuration

  private var lastReportingJob: Option[Cancellable] = None

  private def uninitializedState(alreadyInitializing: Boolean, pendingGetRequests: Vector[ActorRef], pendingUpdateRequests: Vector[(ActorRef, UpdateAggregateRoot)]): Receive = {
    case Initialize =>
      context.become(uninitializedState(true, pendingGetRequests, pendingUpdateRequests))
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
              NotFoundProblem(s"""Aggregate root "$managedAggregateRooId" has been deleted.""")))
            moveToDeleted
          } else {
            pendingGetRequests.foreach(_ ! DomainMessages.RequestedAggregateRoot(ar))
            if (pendingUpdateRequests.isEmpty) {
              moveToIdleState(None, ar)
            } else {
              moveToUpdateState(None, Some(ar), pendingUpdateRequests)
            }
          }
        case None =>
          pendingGetRequests.foreach(_ ! DomainMessages.AggregateRootNotFound(managedAggregateRooId))
          if (pendingUpdateRequests.isEmpty) {
            moveToDoesNotExist
          } else {
            moveToUpdateState(None, None, pendingUpdateRequests)
          }
      }
    case InitializedWithFailure(problem) =>
      pendingGetRequests.foreach(_ ! DomainMessages.AggregateRootFetchFailed(managedAggregateRooId, problem))
      pendingUpdateRequests.map(_._1).foreach(_ ! DomainMessages.AggregateRootUpdateFailed(managedAggregateRooId, problem))
      log.error(s"""Could not initialize aggregate root "$managedAggregateRooId": ${problem.message}""")
      moveToErrorState(problem)
    case GetManagedAggregateRoot =>
      context.become(uninitializedState(alreadyInitializing, pendingGetRequests :+ sender, pendingUpdateRequests))
      if (!alreadyInitializing)
        self ! Initialize
    case uar: UpdateAggregateRoot =>
      context.become(uninitializedState(alreadyInitializing, pendingGetRequests, pendingUpdateRequests :+ (sender, uar)))
      if (!alreadyInitializing)
        self ! Initialize
    case DropCachedAggregateRoot =>
      ()
    case ReportCellState =>
      reportCellState(CellStateUninitialized)
  }

  def idleState(ar: AR, idleSince: LocalDateTime): Receive = {
    case GetManagedAggregateRoot =>
      sender ! RequestedAggregateRoot(ar)
    case uar: UpdateAggregateRoot =>
      moveToUpdateState(Some(ar), Some(ar), Vector((sender, uar)))
    case DropCachedAggregateRoot =>
      moveToUninitialized
    case ReportCellState =>
      reportCellState(CellStateLoaded)
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
          NotFoundProblem(s"""Aggregate root "$managedAggregateRooId" has been deleted.""")))
        moveToDeleted
      } else {
        if (newPendingUpdates.isEmpty) {
          newStateOpt match {
            case Some(newState) =>
              moveToIdleState(arOpt, newState)
            case None =>
              moveToDoesNotExist
          }
        } else {
          moveToUpdateState(arOpt, newStateOpt, newPendingUpdates)
        }
      }
    case FailedUpdate(problem, rest) =>
      log.error(s"""Could not update aggregate root "$managedAggregateRooId": ${problem.message}""")
      (rest ++ pendingUpdateRequests.map(_._1)).foreach(_ ! DomainMessages.AggregateRootUpdateFailed(managedAggregateRooId, problem))
      moveToErrorState(problem)
    case DropCachedAggregateRoot =>
      ()
    case ReportCellState =>
      arOpt match {
        case Some(ar) => reportCellState(CellStateLoaded)
        case None => reportCellState(CellStateUninitialized)
      }
  }

  private def doesNotExistState(isDeleted: Boolean): Receive = {
    case GetManagedAggregateRoot =>
      sender ! AggregateRootNotFound(managedAggregateRooId)
    case uar: UpdateAggregateRoot =>
      if (isDeleted) {
        sender ! AggregateRootUpdateFailed(
          managedAggregateRooId,
          NotFoundProblem(s"""Aggregate root "$managedAggregateRooId" has been deleted."""))
      } else {
        moveToUpdateState(None, None, Vector((sender, uar)))
      }
    case ReportCellState =>
      reportCellState(CellStateDoesNotExist)
    case DropCachedAggregateRoot =>
      ()
  }

  private def errorState(problem: Problem): Receive = {
    case GetManagedAggregateRoot =>
      context.become(uninitializedState(false, Vector(sender), Vector.empty))
      self ! Initialize
    case uar: UpdateAggregateRoot =>
      context.become(uninitializedState(false, Vector.empty, Vector((sender, uar))))
      self ! Initialize
    case DropCachedAggregateRoot =>
      ()
    case ReportCellState =>
      reportCellState(CellStateError(problem))
  }

  //  def rebuildAr(events: Seq[DomainEvent]): AlmValidation[AR] = {
  //    rebuildAggregateRoot(events.map(_.asInstanceOf[Event]))
  //  }

  //  val buildArIteratee = new Iteratee[DomainEvent, Option[AR]] {
  //    def fold[B](folder: Step[DomainEvent, Option[AR]] => Future[B]): Future[B] = {
  //      case 
  //  }
  //}

  //  private def arFolder(step: Step[DomainEvent,Option[AR]]):Future[Option[AR]] = step match {
  //    case Step.Done(a, e) => future(a)
  //    case Step.Cont(k) => future(None)
  //  }

  def fetchAR(): AlmFuture[Option[AR]] = {
    val warnDeadline = Deadline.now
    (domainEventLog ? GetAllDomainEventsFor(managedAggregateRooId))(getArTimeout).successfulAlmFuture[FetchDomainEventsResult].flatMap {
      case FetchedDomainEvents(enumerator) =>
        val iteratee: Iteratee[DomainEvent, Option[AR]] = Iteratee.fold[DomainEvent, Option[AR]](None) {
          case (agg, nextEvent) =>
            val typedDomainEvent = nextEvent.asInstanceOf[Event]
            if (typedDomainEvent.isInstanceOf[CreatesNewAggregateRootEvent] && agg.isEmpty)
              Some(createFreshAggregateRoot(typedDomainEvent).resultOrEscalate)
            else if (agg.isDefined)
              agg.map(x => x.applyEvent(typedDomainEvent).resultOrEscalate)
            else CollisionProblem(s"""There is no aggregate($managedAggregateRooId) root to apply the event ${nextEvent.getClass().getName()} to.""").escalate
        }
        enumerator.onDoneEnumerating(() =>
          warnDeadline.whenTooLate(getArWarnThreshold, elapsed =>
            log.warning(s"""Fetching the events for aggregate root $managedAggregateRooId took more than ${getArWarnThreshold.defaultUnitString}(${elapsed.defaultUnitString}).""")))
        val res = enumerator.run(iteratee)
        res.toSuccessfulAlmFuture
      case FetchDomainEventsFailed(problem) =>
        AlmFuture.failed(problem)
    }.mapTimeout(tp =>
      OperationTimedOutProblem(s"""The domain event log failed to deliver the events for "$managedAggregateRooId" within ${getArTimeout.defaultUnitString}.""", cause = Some(tp)))
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
            val start = Deadline.now
            (domainEventLog ? CommitDomainEvents(nextUpdateEvents))(updateArTimeout).successfulAlmFuture[CommitDomainEventsResult].onComplete(
              problem =>
                problem match {
                  case OperationTimedOutProblem(p) =>
                    val prob = OperationTimedOutProblem(s"""The domain event log failed to append the events for "$managedAggregateRooId" within ${updateArTimeout.defaultUnitString}.""", cause = Some(p))
                    self ! FailedUpdate(prob, requestedNextUpdate +: rest.map(_._1))
                  case prob =>
                    self ! FailedUpdate(prob, requestedNextUpdate +: rest.map(_._1))
                },
              succ => {
                start.whenTooLate(updateArWarnThreshold, elapsed =>
                  log.warning(s"""Storing ${nextUpdateEvents.size} events for aggregate root $managedAggregateRooId took more than ${updateArWarnThreshold.defaultUnitString}(${elapsed.defaultUnitString})."""))
                succ match {
                  case CommitFailed(problem) =>
                    self ! FailedUpdate(problem, requestedNextUpdate +: rest.map(_._1))
                  case NothingCommitted() =>
                    log.warning(s"""No events have been committed for $managedAggregateRooId""")
                    self ! SuccessfulUpdate(Some(nextUpdateState), None, rest)
                  case DomainEventsSuccessfullyCommitted(committedEvents) =>
                    self ! SuccessfulUpdate(Some(nextUpdateState), Some((requestedNextUpdate, nextUpdateState)), rest)
                }
              })

        })
  }

  protected def cancelLastReportingJob() {
    lastReportingJob match {
      case Some(job) =>
        job.cancel
        lastReportingJob = None
      case None =>
        ()
    }
  }

  def moveToDoesNotExist() {
    cancelLastReportingJob()
    context.become(doesNotExistState(false))
    if (cellStateReportingDelay.toMillis == 0L)
      reportCellState(CellStateDoesNotExist)
    else {
      lastReportingJob = Some(context.system.scheduler.scheduleOnce(cellStateReportingDelay)(self ! ReportCellState))
    }
  }

  def moveToDeleted() {
    cancelLastReportingJob()
    context.become(doesNotExistState(false))
    reportCellState(CellStateDoesNotExist)
  }

  def moveToUninitialized() {
    cancelLastReportingJob()
    context.become(uninitializedState(false, Vector.empty, Vector.empty))
    reportCellState(CellStateUninitialized)
  }

  def moveToIdleState(oldState: Option[AR], newState: AR) {
    cancelLastReportingJob()
    context.become(idleState(newState, ccuad.getUtcTimestamp))
    if (oldState.isEmpty)
      reportCellState(CellStateLoaded)
  }

  def moveToUpdateState(oldState: Option[AR], newState: Option[AR], pendingUpdateRequests: Vector[(ActorRef, UpdateAggregateRoot)]) {
    cancelLastReportingJob()
    if (oldState.isEmpty && newState.isDefined)
      reportCellState(CellStateLoaded)
    context.become(updateState(newState, pendingUpdateRequests))
    self ! UpdateAR

  }

  def moveToErrorState(problem: Problem) {
    cancelLastReportingJob()
    reportCellState(CellStateError(problem))
    context.become(errorState(problem))
  }

  //  private def logDebugMessage(currentState: String, msg: String) {
  //    log.debug(s"""Cell for "${managedAggregateRooId}" on state "$currentState": $msg""")
  //
  //  }

  protected def receiveAggregateRootCellMsg = uninitializedState(false, Vector.empty, Vector.empty)

  private case object Initialize
  private case class Initialized(ar: Option[AR])
  private case class InitializedWithFailure(problem: Problem)
  private case object UpdateAR

  private case class SuccessfulUpdate(newState: Option[AR], successToNotify: Option[(ActorRef, AR)], rest: Vector[(ActorRef, UpdateAggregateRoot)])
  private case class FailedUpdate(problem: Problem, rest: Vector[ActorRef])

  private case object ReportCellState
}

class AggregateRootCellImpl[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](
  aggregateRooId: JUUID,
  override val createFreshAggregateRoot: TEvent => DomainValidation[TAR],
  theDomainEventLog: ActorRef,
  reportCellStateSink: AggregateRootCellStateSink,
  override val cellStateReportingDelay: FiniteDuration,
  override val publisher: MessagePublisher,
  override val ccuad: CanCreateUuidsAndDateTimes,
  override val execContext: ExecutionContext,
  override val getArWarnThreshold: FiniteDuration,
  override val updateArWarnThreshold: FiniteDuration,
  override val getArTimeout: FiniteDuration,
  override val updateArTimeout: FiniteDuration) extends AggregateRootCellTemplate with Actor with ActorLogging {

  type AR = TAR
  type Event = TEvent

  override def preStart() {
    super.preStart()
    log.debug(s"""Aggregate root cell for managed aggregate root id "$managedAggregateRooId" is about to start.""")
    reportCellState(AggregateRootCell.CellStateUninitialized)
  }

  override def postStop() {
    cancelLastReportingJob()
  }

  val managedAggregateRooId = aggregateRooId

  protected def domainEventLog: ActorRef = theDomainEventLog

  protected override val reportCellState = reportCellStateSink

  override def receive: Receive = receiveAggregateRootCellMsg
}
  
  