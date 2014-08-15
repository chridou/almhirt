package almhirt.domain

import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.context.AlmhirtContext
import almhirt.streaming.PostOffice
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import scala.util.Success

private[almhirt] object AggregateDroneInternalMessages {
  sealed trait AggregateDroneMessage

  final case class ExecuteCommand(command: AggregateCommand) extends AggregateDroneMessage
  sealed trait ExecuteCommandResponse extends AggregateDroneMessage {
    def commandHeader: CommandHeader
    def committedEvents: Seq[AggregateEvent]
  }
  final case class CommandExecuted(commandHeader: CommandHeader, currentVersion: AggregateRootVersion, committedEvents: Seq[AggregateEvent]) extends ExecuteCommandResponse
  final case class CommandNotExecuted(commandHeader: CommandHeader, committedEvents: Seq[AggregateEvent], problem: Problem) extends ExecuteCommandResponse

  object CommandNotExecuted {
    def apply(commandHeader: CommandHeader, problem: Problem): CommandNotExecuted =
      CommandNotExecuted(commandHeader, Seq.empty, problem)
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
 */
trait AggregateDrone[T <: AggregateRoot, E <: AggregateEvent] { me: Actor with ActorLogging with BuildsAggregateRoot[T, E] ⇒
  import AggregateDroneInternalMessages._
  import almhirt.eventlog.AggregateEventLog._

  //*************
  // User API 
  //*************

  def ahContext: AlmhirtContext
  def aggregateEventLog: ActorRef
  def snapshotStorage: Option[ActorRef]

  def handleCreatingCommand: ConfirmationContext[E] ⇒ AggregateCommand ⇒ Unit
  def handleMutatingCommand: ConfirmationContext[E] ⇒ (AggregateCommand, T) ⇒ Unit

  private object DefaultConfirmationContext extends ConfirmationContext[E] {
    def commit(events: Seq[E]) { self ! Commit(events) }
    def reject(problem: Problem) { self ! Rejected(problem) }
    def unhandled() { self ! Unhandled }

  }

  /** Ends with termination */
  protected def onError(ex: AggregateRootDroneException, command: AggregateCommand, commitedEvents: Seq[E] = Seq.empty) {
    context.parent ! CommandNotExecuted(command.header, commitedEvents, UnspecifiedProblem(s"""Something really bad happened: "${ex.getMessage}". Escalating.""", cause = Some(ex)))
    throw ex
  }

  //*************
  //   Internal 
  //*************

  private case class InternalArBuildResult(ar: AggregateRootState[T])
  private case class InternalBuildArFailed(error: Throwable)

  private sealed trait CommandResult
  private case class Commit(events: Seq[E]) extends CommandResult
  private case class Rejected(problem: Problem) extends CommandResult
  private case object Unhandled extends CommandResult

  private def receiveUninitialized: Receive = {
    case ExecuteCommand(command: AggregateCommand) ⇒
      snapshotStorage match {
        case None ⇒
          context.become(receiveRebuildFromScratch(command))
          aggregateEventLog ! GetAllAggregateEventsFor(command.aggId)
        case Some(snaphots) ⇒
          ???
      }
  }

  private def receiveRebuildFromScratch(command: AggregateCommand): Receive = {
    case FetchedAggregateEvents(eventsEnumerator) ⇒
      val iteratee: Iteratee[AggregateEvent, AggregateRootState[T]] = Iteratee.fold[AggregateEvent, AggregateRootState[T]](NeverExisted) {
        case (acc, event) ⇒
          applyEventLifecycleAgnostic(acc, event.asInstanceOf[E])
      }(ahContext.futuresContext)

      eventsEnumerator.run(iteratee).onComplete {
        case scala.util.Success(arState) ⇒
          self ! InternalArBuildResult(arState)
        case scala.util.Failure(error) ⇒
          self ! InternalBuildArFailed(error)
      }(ahContext.futuresContext)

      context.become(receiveEvaluateRebuildResult(command))

    case GetAggregateEventsFailed(problem) ⇒
      onError(AggregateEventStoreFailedReadingException(command.aggId, "An error has occured fetching the aggregate root events:\n$problem"), command)

    case ExecuteCommand(command: AggregateCommand) ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"Command ${command.header} is currently executued."))
  }

  private def receiveRebuildFromSnapshot(delayedCommand: Option[AggregateCommand], queryPending: Boolean): Receive = {
    case _ ⇒ ()

    case ExecuteCommand(command: AggregateCommand) ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"Command ${command.header} is currently executued."))
  }

  private def receiveEvaluateRebuildResult(command: AggregateCommand): Receive = {
    case InternalArBuildResult(arState) ⇒
      arState match {
        case NeverExisted ⇒
          if (command.aggVersion.value != 0) {
            context.parent ! CommandNotExecuted(command.header, ConstraintViolatedProblem(s"Command must target version 0 for a not yet existing aggregate root. It targets ${command.aggVersion.value}."))
            context.become(receiveAcceptingCreatingCommand)
          } else {
            handleCreatingCommand(DefaultConfirmationContext)(command)
            context.become(receiveWaitingForCreatingCommandResult(command))
          }
        case Alive(ar) ⇒
          if (command.aggVersion != ar.version) {
            context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"Command must target version ${ar.version.value}. It targets ${command.aggVersion.value}."))
            context.become(receiveAcceptingMutatingCommand(ar))
          } else {
            handleMutatingCommand(DefaultConfirmationContext)(command, ar)
            context.become(receiveWaitingForMutatingCommandResult(command, ar))
          }
        case d: Dead ⇒
          context.parent ! CommandNotExecuted(command.header, AggregateRootDeletedProblem(command.aggId))
          context.become(receiveDead(d))
      }

    case InternalBuildArFailed(error: Throwable) ⇒
      onError(RebuildAggregateRootFailedException(command.aggId, "An error has occured rebuilding the aggregate root.", error), command)

    case ExecuteCommand(command: AggregateCommand) ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"Command ${command.header} is currently executued."))
  }

  private def receiveAcceptingCreatingCommand: Receive = {
    case ExecuteCommand(command: AggregateCommand) ⇒
      if (command.aggVersion.value != 0) {
        context.parent ! CommandNotExecuted(command.header, ConstraintViolatedProblem(s"Command must target version 0 for a not yet existing aggregate root. It targets ${command.aggVersion.value}."))
        context.become(receiveAcceptingCreatingCommand)
      } else {
        handleCreatingCommand(DefaultConfirmationContext)(command)
        context.become(receiveWaitingForCreatingCommandResult(command))
      }
  }

  private def receiveAcceptingMutatingCommand(state: T): Receive = {
    case ExecuteCommand(command: AggregateCommand) ⇒
      if (command.aggVersion != state.version) {
        context.parent ! CommandNotExecuted(command.header, ConstraintViolatedProblem(s"Command must target version ${state.version.value}. It targets ${command.aggVersion.value}."))
        context.become(receiveAcceptingMutatingCommand(state))
      } else {
        handleMutatingCommand(DefaultConfirmationContext)(command, state)
        context.become(receiveWaitingForMutatingCommandResult(command, state))
      }
  }

  private def receiveWaitingForCreatingCommandResult(command: AggregateCommand): Receive = {
    case Commit(events) ⇒
      if (events.isEmpty) {
        context.parent ! CommandNotExecuted(command.header, IllegalOperationProblem(s"A creating command must result in at least one event."))
        context.become(receiveAcceptingCreatingCommand)
      } else {
        events.foldLeft[AggregateRootState[T]](NeverExisted) { case (acc, cur) ⇒ applyEventLifecycleAgnostic(acc, cur) } match {
          case postnatalis: Postnatalis[T] ⇒
            context.become(receiveCommitEvents(command, events.head, events.tail, Seq.empty, postnatalis))
            aggregateEventLog ! CommitAggregateEvent(events.head)
          case NeverExisted ⇒
            onError(AggregateRootDeletedException(command.aggId, s"""Command did not result in an aggregate root."""), command)
        }
      }

    case Rejected(problem) ⇒
      context.parent ! CommandNotExecuted(command.header, problem)
      context.become(receiveAcceptingCreatingCommand)

    case Unhandled ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"""Could not handle command of type "${command.getClass().getName()}"."""))
      context.become(receiveAcceptingCreatingCommand)

    case ExecuteCommand(command: AggregateCommand) ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"Command ${command.header} is currently executued."))
  }

  private def receiveWaitingForMutatingCommandResult(command: AggregateCommand, state: T): Receive = {
    case Commit(events) ⇒
      if (events.isEmpty) {
        context.parent ! CommandExecuted(command.header, state.version, events)
        context.become(receiveAcceptingMutatingCommand(state))
      } else {
        val postnatalis = events.foldLeft[Postnatalis[T]](Alive(state)) { case (acc, cur) ⇒ applyEventPostnatalis(acc, cur) }
        context.become(receiveCommitEvents(command, events.head, events.tail, Seq.empty, postnatalis))
        aggregateEventLog ! CommitAggregateEvent(events.head)
      }

    case Rejected(problem) ⇒
      context.parent ! CommandNotExecuted(command.header, problem)
      context.become(receiveAcceptingMutatingCommand(state))

    case Unhandled ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"""Could not handle command of type "${command.getClass().getName()}"."""))
      context.become(receiveAcceptingMutatingCommand(state))

    case ExecuteCommand(command: AggregateCommand) ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"Command ${command.header} is currently executued."))
  }

  private def receiveCommitEvents(command: AggregateCommand, inFlight: E, rest: Seq[E], done: Seq[E], state: Postnatalis[T]): Receive = {
    case AggregateEventCommitted(id) ⇒
      val newDone = done :+ inFlight
      rest match {
        case Seq() ⇒
          state match {
            case Alive(ar) =>
              context.parent ! CommandExecuted(command.header, ar.version, newDone)
              context.become(receiveAcceptingMutatingCommand(ar))
            case d: Dead =>
              context.parent ! CommandExecuted(command.header, d.version , newDone)
              context.become(receiveDead(d))
          }
        case next +: newRest ⇒
          aggregateEventLog ! CommitAggregateEvent(next)
          context.become(receiveCommitEvents(command, next, newRest, newDone, state))
      }
    case AggregateEventNotCommitted(id, problem) ⇒
      onError(AggregateEventStoreFailedWritingException(command.aggId, s"The aggregate event store failed writing:\n$problem"), command, done)

    case ExecuteCommand(command: AggregateCommand) ⇒
      context.parent ! CommandNotExecuted(command.header, UnspecifiedProblem(s"Command ${command.header} is currently executued."))
  }

  private def receiveDead(state: Dead): Receive = {
    case ExecuteCommand(command: AggregateCommand) ⇒
      context.parent ! CommandNotExecuted(command.header, AggregateRootDeletedProblem(command.aggId))
  }

  def receive: Receive = receiveUninitialized
}