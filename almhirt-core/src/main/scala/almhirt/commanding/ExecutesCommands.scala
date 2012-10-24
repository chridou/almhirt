package almhirt.commanding

trait ExecutesCommands {
  def executeCommand(com: DomainCommand, ticket: Option[String]): Unit
}