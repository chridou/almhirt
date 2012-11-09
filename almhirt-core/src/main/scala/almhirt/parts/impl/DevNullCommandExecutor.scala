package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.parts.HasCommandHandlers
import almhirt.NotFoundProblem
import almhirt.commanding._
import almhirt.parts.CommandExecutor
import almhirt.environment._
import almhirt.util.TrackingTicket
import akka.util.Duration

class DevNullCommandExecutor(implicit context: AlmhirtContext) extends CommandExecutor {
  import akka.actor._
  private implicit val executionContext = context.system.futureDispatcher
  val actor = context.system.actorSystem.actorOf(Props(new Actor { def receive: Receive = { case _ => () } }))
  def addHandler(handler: HandlesCommand) {}
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {}
  def getHandlerByType(commandType: Class[_ <: DomainCommand])(implicit atMost: Duration): AlmFuture[HandlesCommand] = AlmPromise.failed[HandlesCommand](NotFoundProblem("DevNullCommandHandlerRegistry has no commands"))
  def executeCommand(commandEnvelope: CommandEnvelope) {}
}