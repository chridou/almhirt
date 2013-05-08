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
import almhirt.core.Almhirt

/**
 */
class JustFireCommandExecutorActor(handlers: HasCommandHandlers, repositories: HasRepositories)(implicit theAlmhirt: Almhirt) extends Actor {

  private def executeCommand(command: Command, ticket: Option[TrackingTicket]) {
    ticket foreach { t => theAlmhirt.publishOperationState(InProcess(t, CommandInfo(command))) }
    handlers.getHandlerByType(command.getClass).fold(
      fail => {
        theAlmhirt.publishProblem(fail)
        ticket match {
          case Some(t) => theAlmhirt.publishOperationState(NotExecuted(t, fail))
          case None => ()
        }
      },
      handler => handler.handle(command, ticket))
  }

  def receive: Receive = {
    case commandEnvelope: CommandEnvelope => executeCommand(commandEnvelope.command, commandEnvelope.ticket)
  }
}

class CommandExecutorActorHull(val actor: ActorRef)(implicit almhirt: Almhirt) extends CommandExecutor {
  private implicit val executionContext = almhirt.executionContext
  def executeCommand(commandEnvelope: CommandEnvelope) { actor ! commandEnvelope }
}