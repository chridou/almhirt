package almhirt.tracking

import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.problem.ProblemCause

trait CommandStatusChanged extends SystemEvent {
  def commandHeader: CommandHeader
}

final case class CommandExecutionStarted(header: EventHeader, commandHeader: CommandHeader) extends CommandStatusChanged
final case class CommandExecuted(header: EventHeader, commandHeader: CommandHeader) extends CommandStatusChanged
final case class CommandNotExecuted(header: EventHeader, commandHeader: CommandHeader, cause: ProblemCause) extends CommandStatusChanged


object CommandExecutionStarted {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecutionStarted =
    CommandExecutionStarted(EventHeader(), command.header)
}

object CommandExecuted {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecuted =
    CommandExecuted(EventHeader(), command.header)
}

object CommandNotExecuted {
  def apply(command: Command, cause: ProblemCause)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandNotExecuted =
    CommandNotExecuted(EventHeader(), command.header, cause)
}