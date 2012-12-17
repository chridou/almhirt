package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.parts.HasCommandHandlers
import almhirt.commanding._
import almhirt.parts.CommandExecutor
import almhirt.environment._
import almhirt.util.TrackingTicket
import akka.util.Duration
import almhirt.common.AlmFuture
import almhirt.common.AlmPromise

class DevNullCommandExecutor(implicit baseOps: AlmhirtBaseOps, system: AlmhirtSystem) extends CommandExecutor {
  import akka.actor._
  private implicit val executionContext = baseOps.futureDispatcher
  val actor = system.actorSystem.actorOf(Props(new Actor { def receive: Receive = { case _ => () } }))
  def addHandler(handler: HandlesCommand) {}
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {}
  def getHandlerByType(commandType: Class[_ <: DomainCommand])(implicit atMost: Duration): AlmFuture[HandlesCommand] = AlmPromise.failed[HandlesCommand](NotFoundProblem("DevNullCommandHandlerRegistry has no commands"))
  def executeCommand(commandEnvelope: CommandEnvelope) {}
}