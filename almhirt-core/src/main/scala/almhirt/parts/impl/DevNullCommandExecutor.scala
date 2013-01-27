package almhirt.parts.impl

import scala.concurrent.duration.Duration
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.parts.HasCommandHandlers
import almhirt.commanding._
import almhirt.parts.CommandExecutor
import almhirt.environment._
import almhirt.util.TrackingTicket
import almhirt.common.AlmFuture
import almhirt.core.HasActorSystem

class DevNullCommandExecutor(implicit hasSystem: HasActorSystem) extends CommandExecutor {
  import akka.actor._
  val actor = hasSystem.actorSystem.actorOf(Props(new Actor { def receive: Receive = { case _ => () } }))
  def addHandler(handler: HandlesCommand) {}
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {}
  def getHandlerByType(commandType: Class[_ <: DomainCommand])(implicit atMost: Duration): AlmFuture[HandlesCommand] = AlmFuture.failed[HandlesCommand](NotFoundProblem("DevNullCommandHandlerRegistry has no commands"))
  def executeCommand(commandEnvelope: CommandEnvelope) {}
}

object DevNullCommandExecutor {
  def apply()(implicit hasSystem: HasActorSystem): CommandExecutor = new DevNullCommandExecutor
}