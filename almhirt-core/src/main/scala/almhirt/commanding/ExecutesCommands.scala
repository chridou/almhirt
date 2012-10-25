package almhirt.commanding

trait ExecutesCommands {
  def executeCommand(com: DomainCommand, ticket: Option[String]): Unit
  def executeCommand(commandEnvelope: CommandEnvelope) { executeCommand(commandEnvelope.command, commandEnvelope.ticket) }
}