package almhirt.commanding

trait ExecutesCommands {
  def executeCommand(com: DomainCommand): Unit
}