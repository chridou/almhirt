package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.parts.HasCommandHandlers
import almhirt.commanding.HandlesCommand
import almhirt.NotFoundProblem
import almhirt.commanding.DomainCommand
import almhirt.commanding.ExecutesCommands
import almhirt.parts.CommandExecutor
import almhirt.environment.AlmhirtEnvironment
import almhirt.util.TrackingTicket

class DevNullCommandExecutor(implicit env: AlmhirtEnvironment) extends CommandExecutor {
  import akka.actor._
  val actor = env.context.system.actorSystem.actorOf(Props(new Actor { def receive: Receive = { case _ => () } }))
  def addHandler(handler: HandlesCommand) {}
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {}
  def getHandlerByType(commandType: Class[_ <: DomainCommand]): AlmValidation[HandlesCommand] = NotFoundProblem("DevNullCommandHandlerRegistry has no commands").failure
  def executeCommand(com: DomainCommand, ticket: Option[TrackingTicket]) {}
}