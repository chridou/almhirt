package almhirt.domain

import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.tracking._
import almhirt.streaming._
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import scala.util.Success
import almhirt.problem.{ CauseIsThrowable, CauseIsProblem, HasAThrowable }

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

/**
 * Mix in this trait to create an Actor that manages command execution for an aggregate root and commits the resulting events.
 *  The resulting Actor is intended to be used and managed by the AgrregateRootHive.
 *
 *  Simply send an [[AggregateRootCommand]] to the drone to have it executed.
 *  The drone can only execute on command at a time.
 */
trait AggregateRootDrone[T <: AggregateRoot, E <: AggregateRootEvent] extends StateChangingActorContractor[Event] {
  me: ActorLogging with AggregateRootEventHandler[T, E]  ⇒
  import AggregateRootDroneInternalMessages._
  import almhirt.eventlog.AggregateEventLog._

  type TPayload = Any

  //*************
  // User API 
  //*************

  def futuresContext: ExecutionContext
  def aggregateEventLog: ActorRef
  def snapshotStorage: Option[ActorRef]
  def eventsBroker: StreamBroker[Event]
  implicit def ccuad: CanCreateUuidsAndDateTimes

  def sendMessage(msg: AggregateDroneMessage) {
    context.parent ! msg
  }

  def handleAggregateCommand: ConfirmationContext[E] ⇒ (AggregateRootCommand, AggregateRootLifecycle[T]) ⇒ Unit

  private object DefaultConfirmationContext extends ConfirmationContext[E] {
    def commit(events: Seq[E]) { self ! Commit(events) }
    def reject(problem: Problem) { self ! Rejected(problem) }
    def unhandled() { self ! Unhandled }
  }

  //*************
  //   Internal 
  //*************

  private case class InternalArBuildResult(ar: AggregateRootLifecycle[T])
  private case class InternalBuildArFailed(error: Throwable)

  private sealed trait CommandResult
  private case class Commit(events: Seq[E]) extends CommandResult
  private case class Rejected(problem: Problem) extends CommandResult
  private case object Unhandled extends CommandResult

  private def receiveUninitialized: Receive = {
    case firstCommand: AggregateRootCommand ⇒
     signContractAndThen(eventsBroker, initialPayload = Some(Seq.empty)) {
        case unexpectedCommand: AggregateRootCommand =>
          sendMessage(Busy(unexpectedCommand))
      }(receiveWaitForContract(firstCommand))
  }

  private def receiveWaitForContract(currentCommand: AggregateRootCommand): Receive = {
    case ReadyForDeliveries(_) ⇒
      snapshotStorage match {
        case None ⇒
          context.become(receiveRebuildFromScratch(currentCommand))
          aggregateEventLog ! GetAllAggregateEventsFor(currentCommand.aggId)
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
  }

  private def receiveRebuildFromScratch(currentCommand: AggregateRootCommand): Receive = {
    case FetchedAggregateEvents(eventsEnumerator) ⇒
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

    case GetAggregateEventsFailed(problem) ⇒
      onError(AggregateEventStoreFailedReadingException(currentCommand.aggId, "An error has occured fetching the aggregate root events:\n$problem"), currentCommand, Seq.empty)

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

  private def receiveWaitingForCommandResult(currentCommand: AggregateRootCommand, persistedState: AggregateRootLifecycle[T]): Receive = {
    case Commit(events) if events.isEmpty ⇒
      handleCommandExecuted(persistedState, currentCommand)

    case Commit(events) ⇒
      events.foldLeft[AggregateRootLifecycle[T]](persistedState) { case (acc, cur) ⇒ applyEventLifecycleAgnostic(acc, cur) } match {
        case postnatalis: Postnatalis[T] ⇒
          context.become(receiveCommitEvents(currentCommand, events.head, events.tail, Seq.empty, postnatalis))
          aggregateEventLog ! CommitAggregateEvent(events.head)
        case Vacat ⇒
          val problem = ConstraintViolatedProblem(s"""Command with events did not result in Postnatalis. This might be an error in your command handler.""")
          handleCommandFailed(persistedState, currentCommand, problem)
      }

    case Rejected(problem) ⇒
      handleCommandFailed(persistedState, currentCommand, problem)

    case Unhandled ⇒
      handleCommandFailed(persistedState, currentCommand, UnspecifiedProblem(s"""Could not handle command of type "${currentCommand.getClass().getName()}"."""))

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }


  private def receiveCommitEvents(currentCommand: AggregateRootCommand, inFlight: E, rest: Seq[E], done: Seq[E], unpersisted: Postnatalis[T]): Receive = {
    case AggregateEventCommitted(id) ⇒
      val newDone = done :+ inFlight
      rest match {
        case Seq() ⇒
          this.offerAndThen(newDone, None) {
            case unexpectedCommand: AggregateRootCommand =>
              sendMessage(Busy(unexpectedCommand))
          }(receiveWaitingForEventsDispatched(currentCommand, unpersisted, newDone))
        case next +: newRest ⇒
          aggregateEventLog ! CommitAggregateEvent(next)
          context.become(receiveCommitEvents(currentCommand, next, newRest, newDone, unpersisted))
      }

    case AggregateEventNotCommitted(id, problem) ⇒
      onError(AggregateEventStoreFailedWritingException(currentCommand.aggId, s"The aggregate event store failed writing:\n$problem"), currentCommand, done)

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(Busy(unexpectedCommand))
  }

  private def receiveWaitingForEventsDispatched(currentCommand: AggregateRootCommand, persisted: Postnatalis[T], committedEvents: Seq[E]): Receive = {
    case DeliveryResult(d: DeliveryJobDone, payload) ⇒
      handleCommandExecuted(persisted, currentCommand)

    case DeliveryResult(DeliveryJobFailed(problem, _), payload) ⇒
      onError(CouldNotDispatchAllAggregateEventsException(currentCommand), currentCommand, committedEvents)

    case unexpectedCommand: AggregateRootCommand ⇒
      sendMessage(CommandNotExecuted(unexpectedCommand, UnspecifiedProblem(s"Command ${currentCommand.header} is currently executed.")))
      context.become(receiveWaitingForEventsDispatched(currentCommand, persisted, committedEvents))
  }

  private def receiveMortuus(state: Mortuus): Receive = {
    case commandNotToExecute: AggregateRootCommand ⇒
      handleCommandFailed(state, commandNotToExecute, AggregateRootDeletedProblem(commandNotToExecute.aggId))
  }
  
  def receive: Receive = receiveUninitialized

  /** Ends with termination */
  private def onError(ex: AggregateRootDomainException, currentCommand: AggregateRootCommand, commitedEvents: Seq[E]) {
    log.error(s"Escalating! Something terrible happened:\n$ex")
    sendMessage(CommandNotExecuted(currentCommand, UnspecifiedProblem(s"""Something really bad happened: "${ex.getMessage}". Escalating.""", cause = Some(ex))))
    throw ex
  }

  private def toExecutionFailedByRejectionEvent(commandNotToExecute: AggregateRootCommand, currentCommand: AggregateRootCommand): CommandStatusChanged = {
    val msg = s"""Rejecting command ${commandNotToExecute.getClass().getName()}(${commandNotToExecute.header}) because currently I'm executing ${currentCommand.getClass().getName()}(${currentCommand.header}). The aggregate root id is "${commandNotToExecute.aggId.value}"."""
    CommandExecutionFailed(commandNotToExecute, CollisionProblem(msg))
  }

  private def handleCommandFailed(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand, prob: Problem) {
    if (log.isDebugEnabled)
      log.debug(s"Command ${command.getClass().getName()}(${command.header}) failed:\n$prob")
    sendMessage(CommandNotExecuted(command, prob))
    context.become(receiveAcceptingCommand(persistedState))
  }

  private def handleCommandExecuted(persistedState: AggregateRootLifecycle[T], command: AggregateRootCommand) {
    sendMessage(CommandExecuted(command))
    context.become(receiveAcceptingCommand(persistedState))
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    cancelContract()
  }

  override def postStop() {
    super.postStop()
    cancelContract()
  }
}

private[almhirt] object AggregateRootDroneInternalMessages {
  sealed trait AggregateDroneMessage

  sealed trait ExecuteCommandResponse extends AggregateDroneMessage {
    def command: Command
    def isSuccess: Boolean
  }
  final case class CommandExecuted(command: AggregateRootCommand) extends ExecuteCommandResponse {
    def isSuccess = true
  }
  final case class CommandNotExecuted(command: AggregateRootCommand, problem: Problem) extends ExecuteCommandResponse {
    def isSuccess = false
  }

  final case class Busy(command: AggregateRootCommand) extends ExecuteCommandResponse {
    def isSuccess = false
  }
  
}
