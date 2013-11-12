package almhirt.commanding

import java.util.{ UUID => JUUID }
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
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandExecuted =
    command match {
      case cmd: DomainCommand =>
        CommandExecuted(EventHeader(Map("aggregate-root-id" -> cmd.targettedAggregateRootId.toString(), "aggregate-root-version" -> cmd.targettedVersion.toString())), command.commandId)
      case cmd =>
        CommandExecuted(EventHeader(), command.commandId)
    }
}

object CommandNotExecuted {
  def apply(commandId: JUUID, reason: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandNotExecuted =
    CommandNotExecuted(EventHeader(), commandId, reason: Problem)
  def apply(command: Command, reason: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandNotExecuted =
    command match {
      case cmd: DomainCommand =>
        CommandNotExecuted(EventHeader(Map("aggregate-root-id" -> cmd.targettedAggregateRootId.toString(), "aggregate-root-version" -> cmd.targettedVersion.toString())), command.commandId, reason)
      case cmd =>
        CommandNotExecuted(EventHeader(), command.commandId, reason)
    }
}