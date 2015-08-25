package almhirt.tracking

import java.time.LocalDateTime
import almhirt.common._
import almhirt.problem.ProblemCause

final case class CommandStatusChanged(
  header: EventHeader,
  commandHeader: CommandHeader,
  status: CommandStatus) extends SystemEvent

object CommandStatusChanged {
  def apply(commandHeader: CommandHeader, status: CommandStatus)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandStatusChanged =
    CommandStatusChanged(EventHeader(), commandHeader, status)
}

object CommandExecutionInitiated {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandStatusChanged =
    command match {
      case cmd: AggregateRootCommand ⇒
        CommandStatusChanged(
          EventHeader().withMetadata(Map("aggregate-id" → s"${cmd.aggId.value}", "aggregate-version" → s"${cmd.aggVersion.value}")),
          command.header,
          CommandStatus.Initiated)
      case cmd ⇒
        CommandStatusChanged(
          EventHeader(),
          command.header,
          CommandStatus.Initiated)
    }
}

object CommandSuccessfullyExecuted {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandStatusChanged =
    command match {
      case cmd: AggregateRootCommand ⇒
        CommandStatusChanged(
          EventHeader().withMetadata(Map("aggregate-id" → s"${cmd.aggId.value}", "aggregate-version" → s"${cmd.aggVersion.value}")),
          command.header,
          CommandStatus.Executed)
      case cmd ⇒
        CommandStatusChanged(
          EventHeader(),
          command.header,
          CommandStatus.Executed)
    }
}

object CommandExecutionFailed {
  def apply(command: Command, cause: ProblemCause)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandStatusChanged =
    command match {
      case cmd: AggregateRootCommand ⇒
        CommandStatusChanged(
          EventHeader().withMetadata(Map("aggregate-id" → s"${cmd.aggId.value}", "aggregate-version" → s"${cmd.aggVersion.value}")),
          command.header,
          CommandStatus.NotExecuted(cause))
      case cmd ⇒
        CommandStatusChanged(
          EventHeader(),
          command.header,
          CommandStatus.NotExecuted(cause))
    }
}