package almhirt.commanding

import almhirt.util.TrackingTicket

trait ExecutesCommandCmd
case class ExecuteCommandCmd(commandEnvelope: CommandEnvelope) extends ExecutesCommandCmd

trait ExecutesCommands {
  def executeCommand(com: DomainCommand, ticket: Option[TrackingTicket]): Unit
  def executeCommand(commandEnvelope: CommandEnvelope) { executeCommand(commandEnvelope.command, commandEnvelope.ticket) }
}