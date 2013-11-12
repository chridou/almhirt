package almhirt.commanding

import almhirt.common._

/**
 * Anyone who gets his hands on a command may tell this with this Event.
 * Additional information contained in the meta data is always welcome
 */
trait CommandReceivedEvent extends Event {
  override def changeMetadata(newMetadata: Map[String, String]): CommandReceivedEvent
}

object CommandReceivedEvent {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceivedEvent =
    command match {
      case cmd: DomainCommand =>
        CommandReceived(EventHeader(Map("aggregate-root-id" -> cmd.targettedAggregateRootId.toString(), "aggregate-root-version" -> cmd.targettedVersion.toString())), command)
      case cmd =>
        CommandReceived(EventHeader(), command)
    }
  def apply(commandHeader: CommandHeader, commandType: String)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceivedEvent =
    apply(EventHeader(), commandHeader, commandType)
  def apply(header: EventHeader, commandHeader: CommandHeader, commandType: String)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceivedEvent =
    CommandReceivedAsHeader(header, commandHeader, commandType)
}

final case class CommandReceived(val header: EventHeader, val command: Command) extends CommandReceivedEvent {
  override def changeMetadata(newMetaData: Map[String, String]): CommandReceived = copy(header = this.header.changeMetadata(newMetaData))
}

object CommandReceived {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceived =
    command match {
      case cmd: DomainCommand =>
        CommandReceived(EventHeader(Map("aggregate-root-id" -> cmd.targettedAggregateRootId.toString(), "aggregate-root-version" -> cmd.targettedVersion.toString())), command)
      case cmd =>
        CommandReceived(EventHeader(), command)
    }
}

final case class CommandReceivedAsHeader(val header: EventHeader, val commandHeader: CommandHeader, commandType: String) extends CommandReceivedEvent {
  override def changeMetadata(newMetaData: Map[String, String]): CommandReceivedAsHeader = copy(header = this.header.changeMetadata(newMetaData))
}

object CommandReceivedAsHeader {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceivedAsHeader =
    command match {
      case cmd: DomainCommand =>
        CommandReceivedAsHeader(EventHeader(Map("aggregate-root-id" -> cmd.targettedAggregateRootId.toString(), "aggregate-root-version" -> cmd.targettedVersion.toString())), command)
      case cmd =>
        CommandReceivedAsHeader(EventHeader(), command)
    }
  def apply(header: EventHeader, command: Command): CommandReceivedAsHeader =
    CommandReceivedAsHeader(header, command.header, command.getClass().getName())
}