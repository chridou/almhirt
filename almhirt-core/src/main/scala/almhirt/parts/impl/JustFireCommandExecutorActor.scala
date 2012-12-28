package almhirt.parts.impl

import scala.concurrent.duration.Duration
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.core._
import almhirt.almfuture.all._
import almhirt.messaging._
import almhirt.parts._
import almhirt.environment._
import almhirt.commanding._
import almhirt.parts.CommandExecutor
import almhirt.util._
import almhirt.common.AlmFuture

/**
 */
class JustFireCommandExecutorActor(handlers: HasCommandHandlers, repositories: HasRepositories)(implicit baseOps: AlmhirtBaseOps) extends Actor {

  private def executeCommand(command: DomainCommand, ticket: Option[TrackingTicket]) {
    ticket foreach { t => baseOps.reportOperationState(InProcess(t)) }
    handlers.getHandlerByType(command.getClass).fold(
      fail => {
        baseOps.reportProblem(fail)
        ticket match {
          case Some(t) => baseOps.reportOperationState(NotExecuted(t, fail))
          case None => ()
        }
      },
      handler => handler.handle(command, ticket))
  }

  def receive: Receive = {
    case commandEnvelope: CommandEnvelope => executeCommand(commandEnvelope.command, commandEnvelope.ticket)
  }
}

class CommandExecutorActorHull(val actor: ActorRef, context: AlmhirtContext) extends CommandExecutor {
  private implicit val executionContext = context.executionContext
  def executeCommand(commandEnvelope: CommandEnvelope) { actor ! commandEnvelope }
}