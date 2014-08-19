package almhirt.tracking

import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.problem.ProblemCause

trait CommandStatusChanged extends SystemEvent {
  def commandHeader: CommandHeader
}

final case class CommandExecutionStarted(header: EventHeader, commandHeader: CommandHeader) extends CommandStatusChanged
final case class CommandSuccessfullyExecuted(header: EventHeader, commandHeader: CommandHeader) extends CommandStatusChanged
final case class CommandFailed(header: EventHeader, commandHeader: CommandHeader, cause: ProblemCause) extends CommandStatusChanged

object CommandExecutionStarted {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecutionStarted =
    command match {
      case cmd: AggregateCommand =>
        CommandExecutionStarted(EventHeader().withMetadata(Map("aggregate-id" -> s"${cmd.aggId.value}", "aggregate-version" -> s"${cmd.aggVersion.value}")), command.header)
      case cmd =>
        CommandExecutionStarted(EventHeader(), command.header)
    }
}

object CommandSuccessfullyExecuted {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandSuccessfullyExecuted =
    command match {
      case cmd: AggregateCommand =>
        CommandSuccessfullyExecuted(EventHeader().withMetadata(Map("aggregate-id" -> s"${cmd.aggId.value}", "aggregate-version" -> s"${cmd.aggVersion.value}")), command.header)
      case cmd =>
        CommandSuccessfullyExecuted(EventHeader(), command.header)
    }
}

object CommandFailed {
  def apply(command: Command, cause: ProblemCause)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandFailed =
    command match {
      case cmd: AggregateCommand =>
        CommandFailed(EventHeader().withMetadata(Map("aggregate-id" -> s"${cmd.aggId.value}", "aggregate-version" -> s"${cmd.aggVersion.value}")), command.header, cause)
      case cmd =>
        CommandFailed(EventHeader(), command.header, cause)
    }
}