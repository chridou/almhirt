package almhirt.commanding

trait ExecutesCommandCmd
case class ExecuteCommandCmd(commandEnvelope: CommandEnvelope) extends ExecutesCommandCmd

trait ExecutesCommands {
  def executeCommand(com: DomainCommand, ticket: Option[String]): Unit
  def executeCommand(commandEnvelope: CommandEnvelope) { executeCommand(commandEnvelope.command, commandEnvelope.ticket) }
}