package almhirt.tracking

import almhirt.common._
import org.joda.time.LocalDateTime

/**
 * Anyone who gets his hands on a command may tell this with this Event.
 * Additional information contained in the meta data is always welcome
 */
final case class CommandReceived(id: EventId, timestamp: LocalDateTime, commandId: CommandId, metadata: Map[String, String]) extends SystemEvent

object CommandReceived {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceived =
    CommandReceived(EventId(ccuad.getUniqueString), ccuad.getUtcTimestamp, command.id, Map.empty)
}
