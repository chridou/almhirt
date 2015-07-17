package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.snapshots._
import almhirt.aggregates._
import almhirt.tracking._
import almhirt.problem.{ CauseIsThrowable, CauseIsProblem, HasAThrowable }
import almhirt.almvalidation.kit._
import almhirt.context.AlmhirtContext
import almhirt.streaming._
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import scala.util.Success
import almhirt.domain.AggregateRootHiveInternals._

final case class SnapshottingForDrone(storage: ActorRef, policy: SnapshottingPolicy) {
  def requiredActionFor(newState: AggregateRootLifecycle[_ <: AggregateRoot], lastSnapshotState: SnapshotState): Option[SnapshotRepository.SnapshottingAction] =
    policy.requiredActionFor(newState, lastSnapshotState)
}

/** Used to commit or reject the events resulting from a command */
trait ConfirmationContext[E <: AggregateRootEvent] {
  /**
   * Call from within handle*Command to apply changes.
   * Do not call multiple times from within command a handler.
   */
  def commit(events: Seq[E])
  /**
   * Call from within handle*Command to reject a command.
   * Do not call multiple times from within command a handler.
   */
  def reject(problem: Problem)
  /**
   * Call from within handle*Command to signal that a command cannot be handled.
   * Do not call multiple times from within command a handler.
   */
  def unhandled()
}

sealed trait PreStoreEventAction
object PreStoreEventAction {
  case object NoAction extends PreStoreEventAction
  final case class AsyncPreStoreAction(action: () ⇒ AlmFuture[Unit]) extends PreStoreEventAction
  final case class PreStoreAction(action: () ⇒ AlmValidation[Unit]) extends PreStoreEventAction
}
object AggregateRootDrone {
  def propsRawMaker(returnToUnitializedAfter: Option[FiniteDuration], maker: Option[FiniteDuration] ⇒ Props): Props = {
    maker(returnToUnitializedAfter)
  }

  def propsMaker(maker: Option[FiniteDuration] ⇒ Props,
                 droneConfigName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val path = "almhirt.components.aggregates.aggregate-root-drone" + droneConfigName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      returnToUnitializedAfter ← section.magicOption[FiniteDuration]("return-to-unitialized-after")
    } yield propsRawMaker(returnToUnitializedAfter, maker)
  }
}

private[almhirt] object AggregateRootDroneInternal {
  case object ReturnToUninitialized
}

/**
 * Mix in this trait to create an Actor that manages command execution for an aggregate root and commits the resulting events.
 *  The resulting Actor is intended to be used and managed by the AgrregateRootHive.
 *
 *  Simply send an [[AggregateRootCommand]] to the drone to have it executed.
 *  The drone can only execute on command at a time.
 */
trait AggregateRootDrone[T <: AggregateRoot, E <: AggregateRootEvent] extends StateChangingActorContractor[Event] {
  me: AggregateRootEventHandler[T, E] ⇒
  import almhirt.eventlog.AggregateRootEventLog._

  implicit protected def arTag: scala.reflect.ClassTag[T]

  type TPayload = Any

  //*************
  // User API 
  //*************

  def futuresContext: ExecutionContext
  def aggregateEventLog: ActorRef
  def snapshotting: Option[SnapshottingForDrone]
  def eventsBroker: StreamBroker[Event]
  def returnToUnitializedAfter: Option[FiniteDuration]
  def notifyHiveAboutUndispatchedEventsAfter: Option[FiniteDuration]
  def notifyHiveAboutUnstoredEventsAfterPerEvent: Option[FiniteDuration]

  def rebuildWarnDuration: Option[FiniteDuration] = Some(0.1.seconds)
  def commandExecutionWarnDuration: Option[FiniteDuration] = Some(0.3.seconds)

  def onBeforeExecutingCommand(cmd: AggregateRootCommand, state: AggregateRootLifecycle[T]): Unit = {

  }

  def onAfterExecutingCommand(cmd: AggregateRootCommand, problem: Option[Problem], state: Option[AggregateRootLifecycle[T]]): Unit = {

  }

  def asyncInitializeForCommand(cmd: AggregateRootCommand, state: AggregateRootLifecycle[T]): Option[AlmFuture[Unit]] = None

  def asyncCleanupAfterCommand(cmd: AggregateRootCommand, problem: Option[Problem], state: Option[AggregateRootLifecycle[T]]): Option[AlmFuture[Unit]] = None

  def logDebug(msg: ⇒ String): Unit = {
    sendMessage(AggregateRootHiveInternals.ReportDroneDebug(msg))
  }

  def logWarning(msg: ⇒ String, cause: Option[almhirt.problem.ProblemCause]): Unit = {
    sendMessage(AggregateRootHiveInternals.ReportDroneWarning(msg, cause))
  }

  def logWarning(msg: ⇒ String): Unit = {
    sendMessage(AggregateRootHiveInternals.ReportDroneWarning(msg, None))
  }

  def logError(msg: ⇒ String, cause: almhirt.problem.ProblemCause): Unit = {
    sendMessage(AggregateRootHiveInternals.ReportDroneError(msg, cause))
  }

  /**
   * None means fail instead of another attempt....
   */
  def retryEventLogActionDelay: Option[FiniteDuration]
  implicit def ccuad: CanCreateUuidsAndDateTimes

  def handleAggregateCommand: ConfirmationContext[E] ⇒ (AggregateRootCommand, AggregateRootLifecycle[T]) ⇒ Unit
  def preStoreActionFor: E ⇒ PreStoreEventAction

  /**
   * Override to perform your own initialization(like resolving dependencies).
   * In case of an error you must call  sendNotExecutedAndEscalate
   *
   * Do not forget to handle any incoming command and to call #signContract" after initializing.
   * Use the default implementation as a template.
   */
  protected case object StartUserInitialization
  def receiveUserInitialization(currentCommand: AggregateRootCommand): Receive = {
    case StartUserInitialization ⇒
      signContract(currentCommand)

    case unexpectedCommand: AggregateRootCommand ⇒
      sendBusy(unexpectedCommand)
  }

  protected final def signContract(currentCommand: AggregateRootCommand) {
    context.become(receiveSignContract(currentCommand))
    self ! SignContract
  }

  protected final def sendBusy(unexpectedCommand: AggregateRootCommand) {
    sendMessage(Busy(unexpectedCommand))
  }

  protected final def sendNotExecutedAndEscalate(currentCommand: AggregateRootCommand, cause: almhirt.problem.ProblemCause) {
    context.parent ! AggregateRootHiveInternals.ReportDroneError("Escalating! Something terrible happened while performing user initialization.", cause)
    sendMessage(CommandNotExecuted(currentCommand, UnspecifiedProblem(s"""Something really bad happened. Escalating.""", cause = Some(cause))))
    throw UserInitializationFailedException("User initialization failed.", cause)
  }

  //*************
  //   Internal 
  //*************

  private case class AsyncInitializedForCommand(cmd: AggregateRootCommand, state: AggregateRootLifecycle[T])
  private case class AsyncInitializeForCommandFailed(cmd: AggregateRootCommand, state: AggregateRootLifecycle[T], problem: Problem)

  private case class AsyncCleanedUpAfterCommand(cmd: AggregateRootCommand, state: AggregateRootLifecycle[T])
  private case class AsyncCleanUpAfterCommandFailed(cmd: AggregateRootCommand, problem: Problem)

  private object DefaultConfirmationContext extends ConfirmationContext[E] {
    def commit(events: Seq[E]) { self ! Commit(events) }
    def reject(problem: Problem) { self ! Rejected(problem) }
    def unhandled() { self ! Unhandled }
  }

  private trait PreStoreEventActionRes
  private case object PreStoreEventActionSucceeded extends PreStoreEventActionRes
  private case class PreStoreEventActionFailed(cause: Problem) extends PreStoreEventActionRes

  /**
   *  Send a message to a stakeholder.
   *  In production the hive has to be notified which should be the parent
   */
  def sendMessage(msg: AggregateDroneMessage) {
    context.parent ! msg
  }

  private case object SignContract

  private case object RetryLogEvent

  private case class InternalArBuildResult(ar: AggregateRootLifecycle[T])
  private case class InternalBuildArFailed(error: Throwable)

  private sealed trait CommandResult
  private case class Commit(events: Seq[E]) extends CommandResult
  private case class Rejected(problem: Problem) extends CommandResult
  private case object Unhandled extends CommandResult

  private case class EventsShouldHaveBeenDispatchedByNow(correlationId: CorrelationId)
  private case class EventsShouldHaveBeenStoredByNow(correlationId: CorrelationId)

  private case object StoreSnapshot
  private case object DispatchEvents

  private var cancelWaitingForDispatchedEventsNotification: Option[Cancellable] = None
  private var cancelWaitingForLoggedEventsNotification: Option[Cancellable] = None

  // My aggregate root id. If Some(id) already received a command and have probalby been initialized...
  private var capturedId: Option[AggregateRootId] = None

  private var lastSnapshotState: SnapshotState = SnapshotState.SnapshotVacat

  private var rebuildStartedOn: Deadline = null
  private var commandStartedOn: Deadline = null

  private def receiveUninitialized: Receive = {
    case firstCommand: AggregateRootCommand ⇒
      capturedId match {
        case Some(id) ⇒
          if (firstCommand.aggId == id) {
            rebuildStartedOn = Deadline.now
            snapshotting match {
              case None ⇒
                context.become(receiveRebuildFrom(firstCommand, Vacat))
                aggregateEventLog ! GetAggregateRootEventsFor(firstCommand.aggId, FromStart, ToEnd, skip.none takeAll)
              case Some(SnapshottingForDrone(snapshotstorage, _)) ⇒
                context.become(receiveRebuildFromSnapshot(firstCommand))
                snapshotstorage ! SnapshotRepository.FindSnapshot(firstCommand.aggId)
            }
          } else {
            sendMessage(CommandNotExecuted(
              firstCommand,
              IllegalOperationProblem(s"""This drone only accepts commands for aggregate root id "${id.value}". The command targets ${firstCommand.aggId.value}.""")))
          }
        case None ⇒
          capturedId = Some(firstCommand.aggId)
          context.become(receiveUserInitialization(firstCommand))
          self ! StartUserInitialization
      }
  }

  private def receiveSignContract(currentCommand: AggregateRootCommand): Receive = {
    case SignContract ⇒
      signContractAndThen(eventsBroker, initialPayload = Some(Seq.empty)) {
        case unexpectedCommand: AggregateRootCommand ⇒
          sendMessage(Busy(unexpectedCommand))
      }(receiveWaitForContract(currentCommand))

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveWaitForContract(currentCommand: AggregateRootCommand): Receive = {
    case ReadyForDeliveries(_) ⇒
      rebuildStartedOn = Deadline.now
      snapshotting match {
        case None ⇒
          context.become(receiveRebuildFrom(currentCommand, Vacat))
          aggregateEventLog ! GetAggregateRootEventsFor(currentCommand.aggId, FromStart, ToEnd, skip.none takeAll)
        case Some(SnapshottingForDrone(snapshotstorage, _)) ⇒
          context.become(receiveRebuildFromSnapshot(currentCommand))
          snapshotstorage ! SnapshotRepository.FindSnapshot(currentCommand.aggId)
      }
    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveAcceptingCommand(persistedState: AggregateRootLifecycle[T], canAccept: Boolean): Receive = {
    case nextCommand: AggregateRootCommand ⇒
      commandStartedOn = Deadline.now
      if (canAccept) {
        asyncInitializeForCommand(nextCommand, persistedState) match {
          case None ⇒
            onBeforeExecutingCommand(nextCommand, persistedState)
            handleAggregateCommand(DefaultConfirmationContext)(nextCommand, persistedState)
            context.become(receiveWaitingForCommandResult(nextCommand, persistedState))
          case Some(fut) ⇒
            context.become(receiveAcceptingCommand(persistedState, false))
            fut.onComplete(
              fail ⇒ self ! AsyncInitializeForCommandFailed(nextCommand, persistedState, fail),
              succ ⇒ self ! AsyncInitializedForCommand(nextCommand, persistedState))(futuresContext)
        }
      } else {
        sendMessage(Busy(nextCommand))
      }

    case AsyncInitializedForCommand(cmd, persistedState) ⇒
      onBeforeExecutingCommand(cmd, persistedState)
      handleAggregateCommand(DefaultConfirmationContext)(cmd, persistedState)
      context.become(receiveWaitingForCommandResult(cmd, persistedState))

    case AsyncInitializeForCommandFailed(cmd, persistedState, problem) ⇒
      handleCommandFailedAfterCleanup(persistedState, cmd, problem)

    case AggregateRootDroneInternal.ReturnToUninitialized ⇒
      if (canAccept) {
        capturedId.foreach { id ⇒ sendMessage(AggregateRootHiveInternals.CargoJettisoned(id)) }
        context.become(receiveUninitialized)
      }

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()
  }

  private def receiveRebuildFrom(currentCommand: AggregateRootCommand, state: AggregateRootLifecycle[T]): Receive = {
    case FetchedAggregateRootEvents(eventsEnumerator) ⇒
      val iteratee: Iteratee[AggregateRootEvent, AggregateRootLifecycle[T]] = Iteratee.fold[AggregateRootEvent, AggregateRootLifecycle[T]](state) {
        case (acc, event) ⇒
          applyEventLifecycleAgnostic(acc, event.asInstanceOf[E])
      }(futuresContext)

      eventsEnumerator.run(iteratee).onComplete {
        case scala.util.Success(arState) ⇒
          self ! InternalArBuildResult(arState)
        case scala.util.Failure(error) ⇒
          self ! InternalBuildArFailed(error)
      }(futuresContext)

      context.become(receiveEvaluateRebuildResult(currentCommand))

    case GetAggregateRootEventsFailed(problem) ⇒
      onError(AggregateRootEventStoreFailedReadingException(currentCommand.aggId, s"An error has occured fetching the aggregate root events:\n$problem"), currentCommand, Seq.empty)

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveRebuildFromSnapshot(currentCommand: AggregateRootCommand): Receive = {
    case SnapshotRepository.FoundSnapshot(untypedAr) ⇒
      untypedAr.castTo[T].fold(
        fail ⇒ {
          logWarning(s"Failed to cast snapshot for ${untypedAr.id.value}. Rebuild all from log.")
          context.become(receiveRebuildFrom(currentCommand, Vacat))
          aggregateEventLog ! GetAggregateRootEventsFor(currentCommand.aggId, FromStart, ToEnd, skip.none takeAll)
        },
        ar ⇒ {
          context.become(receiveRebuildFrom(currentCommand, Vivus(ar)))
          this.lastSnapshotState = SnapshotState.snapshotStateFromLivingAr(ar)
//          logDebug(s"Rebuild from snapshot with version ${ar.version.value}.")
          aggregateEventLog ! GetAggregateRootEventsFor(ar.id, FromVersion(ar.version), ToEnd, skip.none takeAll)
        })

    case SnapshotRepository.SnapshotNotFound(id) ⇒
//      logDebug(s"Snapshot for ${id.value} not found. Rebuild all from log.")
      context.become(receiveRebuildFrom(currentCommand, Vacat))
      aggregateEventLog ! GetAggregateRootEventsFor(currentCommand.aggId, FromStart, ToEnd, skip.none takeAll)

    case SnapshotRepository.AggregateRootWasDeleted(id, version) ⇒
      this.lastSnapshotState = SnapshotState.SnapshotMortuus(version)
      val rsp = CommandNotExecuted(currentCommand, AggregateRootDeletedProblem(id))
      sendMessage(rsp)
      context.become(receiveMortuus(Mortuus(id, version)))

    case SnapshotRepository.FindSnapshotFailed(id, prob) ⇒
      logWarning(s"Failed to load snapshot for ${id.value}. Rebuild all from log.", Some(prob))
      context.become(receiveRebuildFrom(currentCommand, Vacat))
      aggregateEventLog ! GetAggregateRootEventsFor(currentCommand.aggId, FromStart, ToEnd, skip.none takeAll)

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveEvaluateRebuildResult(currentCommand: AggregateRootCommand): Receive = {
    case InternalArBuildResult(arState) ⇒
      rebuildWarnDuration.foreach { rbdur ⇒
        rebuildStartedOn.whenTooLate(rbdur, dur ⇒ logWarning(s"""Rebuild took more than ${rbdur.defaultUnitString}: ${dur.defaultUnitString}"""))
      }
      asyncInitializeForCommand(currentCommand, arState) match {
        case None ⇒
          context.become(receiveWaitingForCommandResult(currentCommand, arState))
          onBeforeExecutingCommand(currentCommand, arState)
          commandStartedOn = Deadline.now
          handleAggregateCommand(DefaultConfirmationContext)(currentCommand, arState)
        case Some(fut) ⇒
          fut.onComplete(
            fail ⇒ self ! AsyncInitializeForCommandFailed(currentCommand, arState, fail),
            succ ⇒ self ! AsyncInitializedForCommand(currentCommand, arState))(futuresContext)
      }

    case AsyncInitializedForCommand(cmd, persistedState) ⇒
      context.become(receiveWaitingForCommandResult(currentCommand, persistedState))
      onBeforeExecutingCommand(currentCommand, persistedState)
      commandStartedOn = Deadline.now
      handleAggregateCommand(DefaultConfirmationContext)(currentCommand, persistedState)

    case AsyncInitializeForCommandFailed(cmd, persistedState, problem) ⇒
      handleCommandFailedAfterCleanup(persistedState, cmd, problem)

    case InternalBuildArFailed(error: Throwable) ⇒
      onError(RebuildAggregateRootFailedException(currentCommand.aggId, "An error has occured rebuilding the aggregate root.", error), currentCommand, Seq.empty)

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def executePreStoreAction(event: E): Unit = {
    preStoreActionFor(event) match {
      case PreStoreEventAction.NoAction ⇒
        self ! PreStoreEventActionSucceeded
      case PreStoreEventAction.PreStoreAction(action) ⇒
        action().fold(
          fail ⇒ self ! PreStoreEventActionFailed(fail),
          succ ⇒ self ! PreStoreEventActionSucceeded)
      case PreStoreEventAction.AsyncPreStoreAction(actionFut) ⇒
        actionFut().onComplete(
          fail ⇒ self ! PreStoreEventActionFailed(fail),
          succ ⇒ self ! PreStoreEventActionSucceeded)(futuresContext)
    }
  }

  private def receiveWaitingForCommandResult(currentCommand: AggregateRootCommand, persistedState: AggregateRootLifecycle[T]): Receive = {
    case Commit(events) if events.isEmpty ⇒
      handleCommandExecutedWithCleanup(persistedState, currentCommand)

    case Commit(events) ⇒
      events.foldLeft[AggregateRootLifecycle[T]](persistedState) { case (acc, cur) ⇒ applyEventLifecycleAgnostic(acc, cur) } match {
        case postnatalis: Postnatalis[T] ⇒
          val cid = CorrelationId(ccuad.getUniqueString())

          notifyHiveAboutUnstoredEventsAfterPerEvent.foreach(delay ⇒
            cancelWaitingForLoggedEventsNotification =
              Some(context.system.scheduler.scheduleOnce(delay * events.size, self, EventsShouldHaveBeenStoredByNow(cid))(context.dispatcher)))

          hiveNotifiedForCommit = false
          context.become(receiveCommitEvents(currentCommand, events.head, events.tail, Seq.empty, postnatalis, Deadline.now, cid))

          executePreStoreAction(events.head)
        case Vacat ⇒
          val problem = ConstraintViolatedProblem(s"""Command with events did not result in Postnatalis. This might be an error in your command handler.""")
          handleCommandFailedWithCleanup(persistedState, currentCommand, problem)
      }

    case Rejected(problem) ⇒
      handleCommandFailedWithCleanup(persistedState, currentCommand, problem)

    case Unhandled ⇒
      val problem = UnspecifiedProblem(s"""Could not handle command of type "${currentCommand.getClass().getName()}".""")
      handleCommandFailedWithCleanup(persistedState, currentCommand, problem)

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  var hiveNotifiedForCommit = false
  private def receiveCommitEvents(
    currentCommand: AggregateRootCommand,
    inFlight: E,
    rest: Seq[E],
    done: Seq[E],
    unpersisted: Postnatalis[T],
    startedStoring: Deadline,
    correlationId: CorrelationId): Receive = {
    case PreStoreEventActionSucceeded ⇒
      aggregateEventLog ! CommitAggregateRootEvent(inFlight)

    case PreStoreEventActionFailed(cause) ⇒
      onError(AggregateRootEventStoreFailedWritingException(currentCommand.aggId, s"Failed to execute PreStoreAction:\n$cause"), currentCommand, done)

    case AggregateRootEventCommitted(id) ⇒
      val newDone = done :+ inFlight
      rest match {
        case Seq() ⇒
          if (hiveNotifiedForCommit) {
            sendMessage(AggregateRootHiveInternals.EventsFinallyLogged(currentCommand.aggId))
            hiveNotifiedForCommit = false
          }

          cancelWaitingForLoggedEventsNotification.foreach { _.cancel() }
          cancelWaitingForLoggedEventsNotification = None

          context.become(receiveStoreSnapshot(currentCommand, unpersisted, newDone))
          self ! StoreSnapshot

        case next +: newRest ⇒
          executePreStoreAction(next)
          context.become(receiveCommitEvents(currentCommand, next, newRest, newDone, unpersisted, startedStoring, correlationId))
      }

    case AggregateRootEventNotCommitted(id, cause) ⇒
      retryEventLogActionDelay match {
        case Some(delay) ⇒
          logWarning(s"Failed to log event ${inFlight.eventId.value} for AR ${inFlight.aggId.value}(Initiate a retry...).", Some(cause))
          context.system.scheduler.scheduleOnce(delay, self, RetryLogEvent)(context.dispatcher)
          context.become(receiveRetryLogEventInFlight(currentCommand, inFlight, rest, done, unpersisted, startedStoring, correlationId))

        case None ⇒
          cancelWaitingForLoggedEventsNotification.foreach { _.cancel() }
          cancelWaitingForLoggedEventsNotification = None
          onError(AggregateRootEventStoreFailedWritingException(currentCommand.aggId, s"The aggregate event store failed writing:\n$cause"), currentCommand, done)
      }

    case EventsShouldHaveBeenStoredByNow(incomingCorrelationId) ⇒
      if (incomingCorrelationId == correlationId) {
        sendMessage(AggregateRootHiveInternals.LogEventsTakesTooLong(currentCommand.aggId, startedStoring.lap))
        context.become(receiveCommitEvents(currentCommand, inFlight, rest, done, unpersisted, startedStoring, correlationId))
        hiveNotifiedForCommit = true
      }

    case EventsShouldHaveBeenDispatchedByNow(_) ⇒
      // Do nothing....
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  def receiveStoreSnapshot(currentCommand: AggregateRootCommand, persisted: Postnatalis[T], eventsToDispatch: Seq[E]): Receive = {
    case StoreSnapshot ⇒
      this.snapshotting match {
        case None ⇒
          context.become(receiveDispatchEvents(currentCommand, persisted, eventsToDispatch))
          self ! DispatchEvents
        case Some(SnapshottingForDrone(snapshotRepo, policy)) ⇒
          policy.requiredActionFor(persisted, lastSnapshotState) match {
            case None ⇒
              context.become(receiveDispatchEvents(currentCommand, persisted, eventsToDispatch))
              self ! DispatchEvents
            case Some(action) ⇒
              snapshotRepo ! action
          }
      }

    case rsp: SnapshotRepository.SuccessfulSnapshottingAction ⇒
      this.lastSnapshotState = SnapshotState.snapshotStateFromLifecycle(persisted)
      context.become(receiveDispatchEvents(currentCommand, persisted, eventsToDispatch))
      self ! DispatchEvents

    case rsp: SnapshotRepository.FailedSnapshottingAction ⇒
      logWarning(s"Failed to store a snapshot for version ${persisted.version.value}.", Some(rsp.problem))
      context.become(receiveDispatchEvents(currentCommand, persisted, eventsToDispatch))
      self ! DispatchEvents

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  def receiveDispatchEvents(currentCommand: AggregateRootCommand, persisted: Postnatalis[T], eventsToDispatch: Seq[E]): Receive = {
    case DispatchEvents ⇒
      val cid = CorrelationId(ccuad.getUniqueString())

      notifyHiveAboutUndispatchedEventsAfter.foreach(delay ⇒
        cancelWaitingForDispatchedEventsNotification =
          Some(context.system.scheduler.scheduleOnce(delay, self, EventsShouldHaveBeenDispatchedByNow(cid))(context.dispatcher)))

      val startOfferEvents = Deadline.now
      hiveNotifiedForDispatch = false
      this.offerAndThen(eventsToDispatch, None) {
        case EventsShouldHaveBeenDispatchedByNow(incomingCorrelationId) ⇒
          if (incomingCorrelationId == cid) {
            sendMessage(AggregateRootHiveInternals.DispatchEventsToStreamTakesTooLong(currentCommand.aggId, startOfferEvents.lap))
            hiveNotifiedForDispatch = true
          }

        case EventsShouldHaveBeenStoredByNow(_) ⇒
          // Do nothing...
          ()

        case unexpectedCommand: AggregateRootCommand ⇒
          sendMessage(Busy(unexpectedCommand))
      }(receiveWaitingForEventsDispatched(currentCommand, persisted, eventsToDispatch, startOfferEvents, cid))

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))

  }

  def receiveWaitForCleanUpAfterExecutedCommand(persistedState: AggregateRootLifecycle[T]): Receive = {
    case AsyncCleanedUpAfterCommand(cmd, persistedState) ⇒
      handleCommandExecutedAfterCleanup(persistedState, cmd)

    case AsyncCleanUpAfterCommandFailed(cmd, problem) ⇒
      logError(s"Cleanup Action after execution a command failed", problem)
      handleCommandExecutedAfterCleanup(persistedState, cmd)

    case EventsShouldHaveBeenDispatchedByNow(_) ⇒
      // Do nothing....
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  def receiveWaitForCleanUpAfterFailedCommand(persistedState: AggregateRootLifecycle[T], commandProblem: Problem): Receive = {
    case AsyncCleanedUpAfterCommand(cmd, persistedState) ⇒
      handleCommandFailedAfterCleanup(persistedState, cmd, commandProblem)

    case AsyncCleanUpAfterCommandFailed(cmd, problem) ⇒
      logError(s"Cleanup Action after a failed command failed", problem)
      handleCommandFailedAfterCleanup(persistedState, cmd, commandProblem)

    case EventsShouldHaveBeenDispatchedByNow(_) ⇒
      // Do nothing....
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveRetryLogEventInFlight(
    currentCommand: AggregateRootCommand,
    inFlight: E,
    rest: Seq[E],
    done: Seq[E],
    unpersisted: Postnatalis[T],
    startedStoring: Deadline,
    correlationId: CorrelationId): Receive = {
    case RetryLogEvent ⇒
      logDebug(s"Retry to log event ${inFlight.eventId.value} for AR ${inFlight.aggId.value}.")
      aggregateEventLog ! GetAggregateRootEvent(inFlight.eventId)

    case FetchedAggregateRootEvent(_, None) ⇒
      logDebug("Retry to log event in event log.")
      // Not already logged? Store it while we can...
      aggregateEventLog ! CommitAggregateRootEvent(inFlight)
      context.become(receiveCommitEvents(currentCommand, inFlight, rest, done, unpersisted, startedStoring, correlationId))

    case FetchedAggregateRootEvent(_, Some(e)) ⇒
      logDebug("Recovered. Event was already stored")
      // Impersonate the event log....
      context.become(receiveCommitEvents(currentCommand, inFlight, rest, done, unpersisted, startedStoring, correlationId))
      self ! AggregateRootEventCommitted(e.eventId)

    case GetAggregateRootEventFailed(eventId, cause) ⇒
      logWarning(s"Could not verify whether event ${inFlight.eventId.value} for AR ${inFlight.aggId.value} was logged(retrying to store the event).", Some(cause))
      retryEventLogActionDelay.foreach { delay ⇒
        context.system.scheduler.scheduleOnce(delay, self, RetryLogEvent)(context.dispatcher)
      }

    case EventsShouldHaveBeenStoredByNow(incomingCorrelationId) ⇒
      if (incomingCorrelationId == correlationId) {
        sendMessage(AggregateRootHiveInternals.LogEventsTakesTooLong(currentCommand.aggId, startedStoring.lap))
        context.become(receiveRetryLogEventInFlight(currentCommand, inFlight, rest, done, unpersisted, startedStoring, correlationId))
      }

    case EventsShouldHaveBeenDispatchedByNow(_) ⇒
      // Do nothing....
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))

  }

  var hiveNotifiedForDispatch = false
  private def receiveWaitingForEventsDispatched(
    currentCommand: AggregateRootCommand,
    persisted: Postnatalis[T],
    committedEvents: Seq[E],
    startedDispatching: Deadline,
    correlationId: CorrelationId): Receive = {
    case DeliveryResult(d: DeliveryJobDone, payload) ⇒
      if (hiveNotifiedForDispatch) {
        sendMessage(AggregateRootHiveInternals.EventsFinallyDispatchedToStream(persisted.id))
        hiveNotifiedForDispatch = false
      }
      cancelWaitingForDispatchedEventsNotification.foreach { _.cancel() }
      cancelWaitingForDispatchedEventsNotification = None
      handleCommandExecutedWithCleanup(persisted, currentCommand)

    case DeliveryResult(DeliveryJobFailed(problem, _), payload) ⇒
      cancelWaitingForDispatchedEventsNotification.foreach { _.cancel() }
      onError(CouldNotDispatchAllAggregateRootEventsException(currentCommand), currentCommand, committedEvents)

    case EventsShouldHaveBeenDispatchedByNow(incomingCorrelationId) ⇒
      if (incomingCorrelationId == correlationId) {
        sendMessage(AggregateRootHiveInternals.DispatchEventsToStreamTakesTooLong(persisted.id, startedDispatching.lap))
        context.become(receiveWaitingForEventsDispatched(currentCommand, persisted, committedEvents, startedDispatching, correlationId))
        hiveNotifiedForDispatch = true
      }

    case EventsShouldHaveBeenStoredByNow(_) ⇒
      // Do nothing....
      ()

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveMortuus(state: Mortuus): Receive = {
    case commandNotToExecute: AggregateRootCommand ⇒
      val rsp = CommandNotExecuted(commandNotToExecute, AggregateRootDeletedProblem(state.id))
      sendMessage(rsp)

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()
  }

  def receive: Receive = receiveUninitialized

  /** Ends with termination */
  private def onError(exn: AggregateRootDomainException, currentCommand: AggregateRootCommand, commitedEvents: Seq[E]) {
    logError(s"Escalating! Something terrible happened executing a command(${currentCommand.getClass.getSimpleName} with agg id ${currentCommand.aggId.value})", exn)
    val rsp = CommandNotExecuted(currentCommand, UnspecifiedProblem(s"""Something really bad happened: "${exn.getMessage}". Escalating.""", cause = Some(exn)))
    sendMessage(rsp)
    onAfterExecutingCommand(currentCommand, Some(rsp.problem), None)
    throw exn
  }

  private def toExecutionFailedByRejectionEvent(commandNotToExecute: AggregateRootCommand, currentCommand: AggregateRootCommand): CommandStatusChanged = {
    val msg = s"""Rejecting command ${commandNotToExecute.getClass().getName()}(${commandNotToExecute.header}) because currently I'm executing ${currentCommand.getClass().getName()}(${currentCommand.header}). The aggregate root id is "${commandNotToExecute.aggId.value}"."""
    CommandExecutionFailed(commandNotToExecute, CollisionProblem(msg))
  }

  private def handleCommandFailedWithCleanup(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand, problem: Problem): Unit = {
    asyncCleanupAfterCommand(command, None, Some(persistedState)) match {
      case None ⇒
        handleCommandFailedAfterCleanup(persistedState, command, problem)
      case Some(fut) ⇒
        context.become(receiveWaitForCleanUpAfterFailedCommand(persistedState, problem))
        fut.onComplete(
          fail ⇒ self ! AsyncCleanUpAfterCommandFailed(command, fail),
          succ ⇒ self ! AsyncCleanedUpAfterCommand(command, persistedState))(futuresContext)
    }
  }

  private def handleCommandFailedAfterCleanup(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand, prob: Problem): Unit = {
    val newProb = persistedState.idOption.fold(prob)(id ⇒ prob.withArg("aggregate-root-id", id.value))
    logDebug(s"Executing a command(${command.getClass.getSimpleName} with agg id ${command.aggId.value}) failed: ${prob.message}")
    val rsp = CommandNotExecuted(command, newProb)
    sendMessage(rsp)
    onAfterExecutingCommand(command, Some(rsp.problem), Some(persistedState))
    becomeReceiveWaitingForCommand(persistedState)
  }

  private def handleCommandExecutedWithCleanup(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand) {
    asyncCleanupAfterCommand(command, None, Some(persistedState)) match {
      case None ⇒
        handleCommandExecutedAfterCleanup(persistedState, command)
      case Some(fut) ⇒
        context.become(receiveWaitForCleanUpAfterExecutedCommand(persistedState))
        fut.onComplete(
          fail ⇒ self ! AsyncCleanUpAfterCommandFailed(command, fail),
          succ ⇒ self ! AsyncCleanedUpAfterCommand(command, persistedState))(futuresContext)
    }
  }

  private def handleCommandExecutedAfterCleanup(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand) {
    commandExecutionWarnDuration.foreach { comdur ⇒
      commandStartedOn.whenTooLate(comdur, dur ⇒ logWarning(s"""Execution of command(including cleanup) "${command.commandId.value}" took more than ${comdur.defaultUnitString}: ${dur.defaultUnitString}"""))
    }
    val rsp = CommandExecuted(command)
    sendMessage(rsp)
    onAfterExecutingCommand(command, None, Some(persistedState))
    becomeReceiveWaitingForCommand(persistedState)
  }

  private def becomeReceiveWaitingForCommand(persistedState: AggregateRootLifecycle[T]) {
    returnToUnitializedAfter.foreach(dur ⇒
      context.system.scheduler.scheduleOnce(dur, self, AggregateRootDroneInternal.ReturnToUninitialized)(context.dispatcher))
    context.become(receiveAcceptingCommand(persistedState, true))
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    logWarning(s"Restarting. Caused by message $message", Some(reason))
    cancelContract()
  }

  override def postStop() {
    super.postStop()
    cancelContract()
  }
}


