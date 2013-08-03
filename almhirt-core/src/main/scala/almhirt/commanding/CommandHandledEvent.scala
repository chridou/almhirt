package almhirt.commanding

import java.util.{UUID => JUUID}
import almhirt.common._

trait CommandHandledEvent extends Event {
  def commandId: JUUID
  override def changeMetadata(newMetaData: Map[String, String]): CommandHandledEvent
}

final case class CommandExecuted(header: EventHeader, commandId: JUUID) extends CommandHandledEvent {
  override def changeMetadata(newMetaData: Map[String, String]): CommandExecuted = copy(header = this.header.changeMetadata(newMetaData))
}

final case class CommandNotExecuted(header: EventHeader, commandId: JUUID, reason: Problem) extends CommandHandledEvent {
  override def changeMetadata(newMetaData: Map[String, String]): CommandNotExecuted = copy(header = this.header.changeMetadata(newMetaData))
}

object CommandExecuted {
  def apply(commandId: JUUID)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecuted =
    CommandExecuted(EventHeader(), commandId)
}

object CommandNotExecuted{
  def apply(commandId: JUUID, reason: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandNotExecuted =
    CommandNotExecuted(EventHeader(), commandId, reason: Problem)
}