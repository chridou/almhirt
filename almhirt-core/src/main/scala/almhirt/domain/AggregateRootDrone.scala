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

private[almhirt] object AggregateRootDroneInternalMessages {
  sealed trait AggregateDroneMessage

  sealed trait ExecuteCommandResponse extends AggregateDroneMessage {
    def commandHeader: CommandHeader
    def isSuccess: Boolean
  }
  final case class CommandExecuted(commandHeader: CommandHeader) extends ExecuteCommandResponse {
    def isSuccess = true
  }
  final case class CommandNotExecuted(commandHeader: CommandHeader, problem: Problem) extends ExecuteCommandResponse {
    def isSuccess = false
  }
 }

/** Used to commit or reject the events resulting from a command */
trait ConfirmationContext[E <: AggregateEvent] {
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
 *  Simply send an [[AggregateCommand]] to the drone to have it executed.
 *  The drone can only execute on command at a time.
 */
trait AggregateRootDrone[T <: AggregateRoot, E <: AggregateEvent] {
  me: Actor with ActorLogging with AggregateRootEventHandler[T, E] with SequentialPostOfficeClient ⇒
  import AggregateRootDroneInternalMessages._
  import almhirt.eventlog.AggregateEventLog._

  //*************
  // User API 
  //*************

  def futuresContext: ExecutionContext
  def aggregateEventLog: ActorRef
  def snapshotStorage: Option[ActorRef]
  def eventsPostOffice: PostOffice[Event]
  def commandStatusSink: FireAndForgetSink[CommandStatusChanged]
  implicit def postOfficeSettings: PostOfficeClientSettings
  implicit def ccuad: CanCreateUuidsAndDateTimes

  def sendMessage(msg: AggregateDroneMessage) {
    context.parent ! msg
  }

  def handleAggregateCommand: ConfirmationContext[E] ⇒ (AggregateCommand, AggregateRootLifecycle[T]) ⇒ Unit

  private object DefaultConfirmationContext extends ConfirmationContext[E] {
    def commit(events: Seq[E]) { self ! Commit(events) }
    def reject(problem: Problem) { self ! Rejected(problem) }
    def unhandled() { self ! Unhandled }
  }

  /** Ends with termination */
  protected final def onError(ex: AggregateRootDomainException, currentCommand: AggregateCommand, commitedEvents: Seq[E] = Seq.empty) {
    log.error(s"Escalating! Something terrible happened:\n$ex")
    sendMessage(CommandNotExecuted(currentCommand.header, UnspecifiedProblem(s"""Something really bad happened: "${ex.getMessage}". Escalating.""", cause = Some(ex))))
    val status = CommandFailed(currentCommand, CauseIsThrowable(HasAThrowable(ex)))
    commandStatusSink(status)
    throw ex
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
    case firstCommand: AggregateCommand ⇒
      commandStatusSink(CommandExecutionStarted(firstCommand))
      snapshotStorage match {
        case None ⇒
          context.become(receiveRebuildFromScratch(firstCommand))
          aggregateEventLog ! GetAllAggregateEventsFor(firstCommand.aggId)
        case Some(snaphots) ⇒
          ???
      }
  }

  private def receiveRebuildFromScratch(currentCommand: AggregateCommand): Receive = {
    case FetchedAggregateEvents(eventsEnumerator) ⇒
      val iteratee: Iteratee[AggregateEvent, AggregateRootLifecycle[T]] = Iteratee.fold[AggregateEvent, AggregateRootLifecycle[T]](Vacat) {
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
      onError(AggregateEventStoreFailedReadingException(currentCommand.aggId, "An error has occured fetching the aggregate root events:\n$problem"), currentCommand)

    case commandNotToExecute: AggregateCommand ⇒
      rejectUnexpectedCommand(commandNotToExecute, currentCommand)
  }

  private def receiveRebuildFromSnapshot(currentCommand: AggregateCommand): Receive = {
    case _ ⇒ ()

    case commandNotToExecute: AggregateCommand ⇒
      sendMessage(CommandNotExecuted(commandNotToExecute.header, UnspecifiedProblem(s"Command ${currentCommand.header} is currently executued.")))
  }

  private def receiveEvaluateRebuildResult(currentCommand: AggregateCommand): Receive = {
    case InternalArBuildResult(arState) ⇒
      context.become(receiveWaitingForCommandResult(currentCommand, arState))
      handleAggregateCommand(DefaultConfirmationContext)(currentCommand, arState)

    case InternalBuildArFailed(error: Throwable) ⇒
      onError(RebuildAggregateRootFailedException(currentCommand.aggId, "An error has occured rebuilding the aggregate root.", error), currentCommand)

    case commandNotToExecute: AggregateCommand ⇒
      rejectUnexpectedCommand(commandNotToExecute, currentCommand)
  }

  private def receiveWaitingForCommandResult(currentCommand: AggregateCommand, persistedState: AggregateRootLifecycle[T]): Receive = {
    case Commit(events) if events.isEmpty ⇒
      context.become(receiveAcceptingCommand(persistedState))
      val version = persistedState match {
        case p: Postnatalis[T] ⇒ p.version
        case _ ⇒ AggregateRootVersion(0L)
      }
      sendCommandAccepted(currentCommand)

    case Commit(events) ⇒
      events.foldLeft[AggregateRootLifecycle[T]](persistedState) { case (acc, cur) ⇒ applyEventLifecycleAgnostic(acc, cur) } match {
        case postnatalis: Postnatalis[T] ⇒
          context.become(receiveCommitEvents(currentCommand, events.head, events.tail, Seq.empty, postnatalis))
          aggregateEventLog ! CommitAggregateEvent(events.head)
        case Vacat ⇒
          val problem = ConstraintViolatedProblem(s"""Command with events did not result in Postnatalis. This might be an error in your command handler.""")
          sendCommandFailed(currentCommand, problem)
          context.become(receiveAcceptingCommand(persistedState))
      }

    case Rejected(problem) ⇒
      sendCommandFailed(currentCommand, problem)
      context.become(receiveAcceptingCommand(persistedState))

    case Unhandled ⇒
      sendCommandFailed(currentCommand, UnspecifiedProblem(s"""Could not handle command of type "${currentCommand.getClass().getName()}"."""))
      context.become(receiveAcceptingCommand(persistedState))

    case commandNotToExecute: AggregateCommand ⇒
      rejectUnexpectedCommand(commandNotToExecute, currentCommand)
  }

  private def receiveAcceptingCommand(persistedState: AggregateRootLifecycle[T]): Receive = {
    case nextCommand: AggregateCommand ⇒
      commandStatusSink(CommandExecutionStarted(nextCommand))
      handleAggregateCommand(DefaultConfirmationContext)(nextCommand, persistedState)
      context.become(receiveWaitingForCommandResult(nextCommand, persistedState))
  }

  private def receiveCommitEvents(currentCommand: AggregateCommand, inFlight: E, rest: Seq[E], done: Seq[E], unpersisted: Postnatalis[T]): Receive = {
    case AggregateEventCommitted(id) ⇒
      val newDone = done :+ inFlight
      rest match {
        case Seq() ⇒
          context.become(receiveWaitingForEventsDispatched(currentCommand, unpersisted, newDone))
          sendToPostOfficeUntrackedWithAppendix(eventsPostOffice, newDone, rejectCommandAppendix(currentCommand))
        case next +: newRest ⇒
          aggregateEventLog ! CommitAggregateEvent(next)
          context.become(receiveCommitEvents(currentCommand, next, newRest, newDone, unpersisted))
      }

    case AggregateEventNotCommitted(id, problem) ⇒
      onError(AggregateEventStoreFailedWritingException(currentCommand.aggId, s"The aggregate event store failed writing:\n$problem"), currentCommand, done)

    case commandNotToExecute: AggregateCommand ⇒
      rejectUnexpectedCommand(commandNotToExecute, currentCommand)
  }

  private def receiveWaitingForEventsDispatched(currentCommand: AggregateCommand, persisted: Postnatalis[T], committedEvents: Seq[E]): Receive = {
    case d: DeliveryJobDone ⇒
      sendCommandAccepted(currentCommand)
      context.become(receiveAcceptingCommand(persisted))
      
    case d: DeliveryJobNotAccepted ⇒
      onError(CouldNotDispatchAllAggregateEventsException(currentCommand), currentCommand, committedEvents)
  }

  private def receiveMortuus(state: Mortuus): Receive = {
    case commandNotToExecute: AggregateCommand ⇒
      sendCommandFailed(commandNotToExecute, AggregateRootDeletedProblem(commandNotToExecute.aggId))
  }

  def receive: Receive = receiveUninitialized

  private def rejectUnexpectedCommand(commandNotToExecute: AggregateCommand, currentCommand: AggregateCommand) {
    val msg = s"""Rejecting command ${commandNotToExecute.getClass().getName()}(${commandNotToExecute.header}) because currently I'm executing ${currentCommand.getClass().getName()}(${currentCommand.header}). The aggregate root id is "${commandNotToExecute.aggId.value}"."""
    sendCommandFailed(commandNotToExecute, CollisionProblem(msg))
  }

  private def sendCommandFailed(command: AggregateCommand, prob: Problem) {
    if (log.isDebugEnabled)
      log.debug(s"Command ${command.getClass().getName()}(${command.header}) failed:\n$prob")
    sendMessage(CommandNotExecuted(command.header, prob))
    commandStatusSink(CommandFailed(command, CauseIsProblem(prob)))
  }

  private def sendCommandAccepted(command: AggregateCommand) {
    sendMessage(CommandExecuted(command.header))
    commandStatusSink(CommandSuccessfullyExecuted(command))
  }

  private def rejectCommandAppendix(currentCommand: AggregateCommand): Receive = {
    case commandNotToExecute: AggregateCommand ⇒
      rejectUnexpectedCommand(commandNotToExecute, currentCommand)
  }

}