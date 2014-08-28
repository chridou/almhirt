package almhirt.tracking

import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.problem.ProblemCause

trait CommandStatusChanged extends SystemEvent {
  def commandHeader: CommandHeader
}

final case class CommandExecutionInitiated(header: EventHeader, commandHeader: CommandHeader) extends CommandStatusChanged

object CommandExecutionInitiated {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecutionInitiated =
    command match {
      case cmd: AggregateRootCommand ⇒
        CommandExecutionInitiated(EventHeader().withMetadata(Map("aggregate-id" -> s"${cmd.aggId.value}", "aggregate-version" -> s"${cmd.aggVersion.value}")), command.header)
      case cmd ⇒
        CommandExecutionInitiated(EventHeader(), command.header)
    }
}

sealed trait ComandExecutionResultChanged extends CommandStatusChanged
final case class CommandSuccessfullyExecuted(header: EventHeader, commandHeader: CommandHeader) extends ComandExecutionResultChanged
final case class CommandExecutionFailed(header: EventHeader, commandHeader: CommandHeader, cause: ProblemCause) extends ComandExecutionResultChanged

object CommandSuccessfullyExecuted {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandSuccessfullyExecuted =
    command match {
      case cmd: AggregateRootCommand ⇒
        CommandSuccessfullyExecuted(EventHeader().withMetadata(Map("aggregate-id" -> s"${cmd.aggId.value}", "aggregate-version" -> s"${cmd.aggVersion.value}")), command.header)
      case cmd ⇒
        CommandSuccessfullyExecuted(EventHeader(), command.header)
    }
}

object CommandExecutionFailed {
  def apply(command: Command, cause: ProblemCause)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecutionFailed =
    command match {
      case cmd: AggregateRootCommand ⇒
        CommandExecutionFailed(EventHeader().withMetadata(Map("aggregate-id" -> s"${cmd.aggId.value}", "aggregate-version" -> s"${cmd.aggVersion.value}")), command.header, cause)
      case cmd ⇒
        CommandExecutionFailed(EventHeader(), command.header, cause)
    }
}