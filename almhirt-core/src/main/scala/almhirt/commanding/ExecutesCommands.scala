package almhirt.commanding

import almhirt.util.TrackingTicket

trait ExecutesCommandCmd
case class ExecuteCommandCmd(commandEnvelope: CommandEnvelope) extends ExecutesCommandCmd

trait ExecutesCommands {
  def executeCommand(com: DomainCommand, ticket: Option[TrackingTicket]){ executeCommand(CommandEnvelope(com, ticket)) }
  def executeCommand(commandEnvelope: CommandEnvelope): Unit
}