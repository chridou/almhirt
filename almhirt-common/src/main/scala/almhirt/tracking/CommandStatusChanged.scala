package almhirt.tracking

import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.problem.ProblemCause

trait CommandStatusChanged extends SystemEvent {
  def commandHeader: CommandHeader
  def metadata: Map[String, String]
}

final case class CommandReceived(header: EventHeader, commandHeader: CommandHeader, metadata: Map[String, String]) extends CommandStatusChanged
final case class CommandExecuted(header: EventHeader, commandHeader: CommandHeader, metadata: Map[String, String]) extends CommandStatusChanged
final case class CommandNotExecuted(header: EventHeader, commandHeader: CommandHeader, cause: ProblemCause, metadata: Map[String, String]) extends CommandStatusChanged

object CommandReceived {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceived =
    CommandReceived(EventHeader(), command.header, Map.empty)
}
object CommandExecuted {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecuted =
    CommandExecuted(EventHeader(), command.header, Map.empty)
}

object CommandNotExecuted {
  def apply(command: Command, cause: ProblemCause)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandNotExecuted =
    CommandNotExecuted(EventHeader(), command.header, cause, Map.empty)
}