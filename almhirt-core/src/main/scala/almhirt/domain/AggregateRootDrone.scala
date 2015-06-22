package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.tracking._
import almhirt.problem.{ CauseIsThrowable, CauseIsProblem, HasAThrowable }
import almhirt.context.AlmhirtContext
import almhirt.streaming._
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import scala.util.Success
import almhirt.domain.AggregateRootHiveInternals._

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
  final case class PreStoreAction(action: () ⇒ AlmFuture[Unit]) extends PreStoreEventAction
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

  type TPayload = Any

  //*************
  // User API 
  //*************

  def futuresContext: ExecutionContext
  def aggregateEventLog: ActorRef
  def snapshotStorage: Option[ActorRef]
  def eventsBroker: StreamBroker[Event]
  def returnToUnitializedAfter: Option[FiniteDuration]
  def notifyHiveAboutUndispatchedEventsAfter: Option[FiniteDuration]
  def notifyHiveAboutUnstoredEventsAfterPerEvent: Option[FiniteDuration]

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

  private var cancelWaitingForDispatchedEventsNotification: Option[Cancellable] = None
  private var cancelWaitingForLoggedEventsNotification: Option[Cancellable] = None

  // My aggregate root id. If Some(id) already received a command and have probalby been initialized...
  private var capturedId: Option[AggregateRootId] = None

  private def receiveUninitialized: Receive = {
    case firstCommand: AggregateRootCommand ⇒
      capturedId match {
        case Some(id) ⇒
          if (firstCommand.aggId == id) {
            snapshotStorage match {
              case None ⇒
                context.become(receiveRebuildFromScratch(firstCommand))
                aggregateEventLog ! GetAggregateRootEventsFor(firstCommand.aggId, FromStart, ToEnd, skip.none takeAll)
              case Some(snaphots) ⇒
                ???
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
      snapshotStorage match {
        case None ⇒
          context.become(receiveRebuildFromScratch(currentCommand))
          aggregateEventLog ! GetAggregateRootEventsFor(currentCommand.aggId, FromStart, ToEnd, skip.none takeAll)
        case Some(snaphots) ⇒
          ???
      }
    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveAcceptingCommand(persistedState: AggregateRootLifecycle[T]): Receive = {
    case nextCommand: AggregateRootCommand ⇒
      handleAggregateCommand(DefaultConfirmationContext)(nextCommand, persistedState)
      context.become(receiveWaitingForCommandResult(nextCommand, persistedState))

    case AggregateRootDroneInternal.ReturnToUninitialized ⇒
      capturedId.foreach { id ⇒ sendMessage(AggregateRootHiveInternals.CargoJettisoned(id)) }
      context.become(receiveUninitialized)

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()
  }

  private def receiveRebuildFromScratch(currentCommand: AggregateRootCommand): Receive = {
    case FetchedAggregateRootEvents(eventsEnumerator) ⇒
      val iteratee: Iteratee[AggregateRootEvent, AggregateRootLifecycle[T]] = Iteratee.fold[AggregateRootEvent, AggregateRootLifecycle[T]](Vacat) {
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
    case _ ⇒ ???
  }

  private def receiveEvaluateRebuildResult(currentCommand: AggregateRootCommand): Receive = {
    case InternalArBuildResult(arState) ⇒
      context.become(receiveWaitingForCommandResult(currentCommand, arState))
      handleAggregateCommand(DefaultConfirmationContext)(currentCommand, arState)

    case InternalBuildArFailed(error: Throwable) ⇒
      onError(RebuildAggregateRootFailedException(currentCommand.aggId, "An error has occured rebuilding the aggregate root.", error), currentCommand, Seq.empty)

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def executePreStoreAction(event: E): Unit = {
    preStoreActionFor(event) match {
      case PreStoreEventAction.NoAction ⇒
        self ! PreStoreEventActionSucceeded
      case PreStoreEventAction.PreStoreAction(actionFut) ⇒
        actionFut().onComplete(
          fail ⇒ self ! PreStoreEventActionFailed(fail),
          succ ⇒ self ! PreStoreEventActionSucceeded)(futuresContext)
    }
  }

  private def receiveWaitingForCommandResult(currentCommand: AggregateRootCommand, persistedState: AggregateRootLifecycle[T]): Receive = {
    case Commit(events) if events.isEmpty ⇒
      handleCommandExecuted(persistedState, currentCommand)

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
          handleCommandFailed(persistedState, currentCommand, problem)
      }

    case Rejected(problem) ⇒
      handleCommandFailed(persistedState, currentCommand, problem)

    case Unhandled ⇒
      handleCommandFailed(persistedState, currentCommand, UnspecifiedProblem(s"""Could not handle command of type "${currentCommand.getClass().getName()}"."""))

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

          val cid = CorrelationId(ccuad.getUniqueString())

          notifyHiveAboutUndispatchedEventsAfter.foreach(delay ⇒
            cancelWaitingForDispatchedEventsNotification =
              Some(context.system.scheduler.scheduleOnce(delay, self, EventsShouldHaveBeenDispatchedByNow(cid))(context.dispatcher)))

          val startOfferEvents = Deadline.now
          hiveNotifiedForDispatch = false
          this.offerAndThen(newDone, None) {
            case EventsShouldHaveBeenDispatchedByNow(incomingCorrelationId) ⇒
              if (incomingCorrelationId == correlationId) {
                sendMessage(AggregateRootHiveInternals.DispatchEventsToStreamTakesTooLong(currentCommand.aggId, startOfferEvents.lap))
                hiveNotifiedForDispatch = true
              }

            case EventsShouldHaveBeenStoredByNow(_) ⇒
              // Do nothing...
              ()

            case unexpectedCommand: AggregateRootCommand ⇒
              sendMessage(Busy(unexpectedCommand))
          }(receiveWaitingForEventsDispatched(currentCommand, unpersisted, newDone, startOfferEvents, cid))
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
      handleCommandExecuted(persisted, currentCommand)

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
      sendMessage(CommandNotExecuted(unexpectedCommand, UnspecifiedProblem(s"Command ${currentCommand.header} is currently executed.")))
      context.become(receiveWaitingForEventsDispatched(currentCommand, persisted, committedEvents, startedDispatching, correlationId))
  }

  private def receiveMortuus(state: Mortuus): Receive = {
    case commandNotToExecute: AggregateRootCommand ⇒
      handleCommandFailed(state, commandNotToExecute, AggregateRootDeletedProblem(commandNotToExecute.aggId))

    case EventsShouldHaveBeenDispatchedByNow(_) | EventsShouldHaveBeenStoredByNow(_) ⇒
      //Do nothing..
      ()
  }

  def receive: Receive = receiveUninitialized

  /** Ends with termination */
  private def onError(exn: AggregateRootDomainException, currentCommand: AggregateRootCommand, commitedEvents: Seq[E]) {
    logError(s"Escalating! Something terrible happened executing a command(${currentCommand.getClass.getSimpleName} with agg id ${currentCommand.aggId.value})", exn)
    sendMessage(CommandNotExecuted(currentCommand, UnspecifiedProblem(s"""Something really bad happened: "${exn.getMessage}". Escalating.""", cause = Some(exn))))
    throw exn
  }

  private def toExecutionFailedByRejectionEvent(commandNotToExecute: AggregateRootCommand, currentCommand: AggregateRootCommand): CommandStatusChanged = {
    val msg = s"""Rejecting command ${commandNotToExecute.getClass().getName()}(${commandNotToExecute.header}) because currently I'm executing ${currentCommand.getClass().getName()}(${currentCommand.header}). The aggregate root id is "${commandNotToExecute.aggId.value}"."""
    CommandExecutionFailed(commandNotToExecute, CollisionProblem(msg))
  }

  private def handleCommandFailed(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand, prob: Problem) {
    val newProb = persistedState.idOption.fold(prob)(id ⇒ prob.withArg("aggregate-root-id", id.value))
    logDebug(s"Executing a command(${command.getClass.getSimpleName} with agg id ${command.aggId.value}) failed: ${prob.message}")
    sendMessage(CommandNotExecuted(command, newProb))
    becomeReceiveWaitingForCommand(persistedState)
  }

  private def handleCommandExecuted(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand) {
    sendMessage(CommandExecuted(command))
    becomeReceiveWaitingForCommand(persistedState)
  }

  private def becomeReceiveWaitingForCommand(persistedState: AggregateRootLifecycle[T]) {
    returnToUnitializedAfter.foreach(dur ⇒
      context.system.scheduler.scheduleOnce(dur, self, AggregateRootDroneInternal.ReturnToUninitialized)(context.dispatcher))
    context.become(receiveAcceptingCommand(persistedState))
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


