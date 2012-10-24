package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.parts.HasCommandHandlers
import almhirt.commanding.HandlesCommand
import almhirt.NotFoundProblem
import almhirt.commanding.DomainCommand
import almhirt.commanding.ExecutesCommands
import almhirt.parts.CommandExecutor

class DevNullCommandExecutor() extends CommandExecutor {
  def addHandler(handler: HandlesCommand) {}
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {}
  def getHandlerByType(commandType: Class[_ <: DomainCommand]): AlmValidation[HandlesCommand] = NotFoundProblem("DevNullCommandHandlerRegistry has no commands").failure 
  def executeCommand(com: DomainCommand, ticket: Option[String]) {}	  
}