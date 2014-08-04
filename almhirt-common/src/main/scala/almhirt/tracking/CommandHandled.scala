package almhirt.tracking

import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.problem.ProblemCause

trait CommandHandled extends SystemEvent {
  def commandId: CommandId
  def metadata: Map[String, String]
}

final case class CommandExecuted(id: EventId, timestamp: LocalDateTime, commandId: CommandId, metadata: Map[String, String]) extends CommandHandled

final case class CommandNotExecuted(id: EventId, timestamp: LocalDateTime, commandId: CommandId, cause: ProblemCause, metadata: Map[String, String]) extends CommandHandled

object CommandExecuted {
  def apply(commandId: CommandId)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecuted =
    CommandExecuted(EventId(ccuad.getUniqueString), ccuad.getUtcTimestamp, commandId, Map.empty)
}

object CommandNotExecuted {
 def apply(commandId: CommandId, cause: ProblemCause)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandNotExecuted =
     CommandNotExecuted(EventId(ccuad.getUniqueString), ccuad.getUtcTimestamp, commandId, cause, Map.empty)
}