package almhirt.commanding

import almhirt.util.TrackingTicket

trait ExecutesCommands {
  def executeCommand(com: DomainCommand, ticket: Option[TrackingTicket]){ executeCommand(CommandEnvelope(com, ticket)) }
  def executeCommand(commandEnvelope: CommandEnvelope): Unit
}