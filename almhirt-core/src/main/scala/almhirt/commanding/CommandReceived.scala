package almhirt.commanding

import almhirt.common._

trait CommandReceivedEvent extends Event {
  override def changeMetadata(newMetadata: Map[String, String]): CommandReceivedEvent
}

object CommandReceivedEvent {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceivedEvent =
    CommandReceived(EventHeader(), command)
  def apply(header: CommandHeader, commandType: String)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceivedEvent =
    CommandReceivedAsHeader(EventHeader(), header, commandType)
}

final case class CommandReceived(val header: EventHeader, val command: Command) extends CommandReceivedEvent {
  override def changeMetadata(newMetaData: Map[String, String]): CommandReceived = copy(header = this.header.changeMetadata(newMetaData))
}

final case class CommandReceivedAsHeader(val header: EventHeader, val commandHeader: CommandHeader, commandType: String) extends CommandReceivedEvent {
  override def changeMetadata(newMetaData: Map[String, String]): CommandReceivedAsHeader = copy(header = this.header.changeMetadata(newMetaData))
}

object CommandReceivedAsHeader {
  def apply(command: Command)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandReceivedAsHeader =
    CommandReceivedAsHeader(EventHeader(), command.header, command.getClass().getName())
}