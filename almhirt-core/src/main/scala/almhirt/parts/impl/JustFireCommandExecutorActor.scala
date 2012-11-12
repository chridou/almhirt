package almhirt.parts.impl

import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Duration
import almhirt._
import almhirt.almfuture.all._
import almhirt.messaging._
import almhirt.parts._
import almhirt.environment._
import almhirt.commanding._
import almhirt.NotFoundProblem
import almhirt.environment.AlmhirtEnvironment
import almhirt.parts.CommandExecutor
import almhirt.util._

/**
 */
class JustFireCommandExecutorActor(repositories: HasRepositories)(implicit context: AlmhirtContext) extends Actor {
  private val handlers: collection.mutable.Map[String, HandlesCommand] = collection.mutable.HashMap.empty

  private def addHandler(handler: HandlesCommand) {
    handlers.put(handler.commandType.getName, handler)
  }

  private def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {
    handlers.remove(commandType.getName)
  }

  private def getHandlerByType(commandType: Class[_ <: DomainCommand]): AlmValidation[HandlesCommand] =
    handlers.get(commandType.getName) match {
      case Some(h) => h.success
      case None => NotFoundProblem("No handler found for command %s".format(commandType.getName), severity = Major).failure
    }

  private def executeCommand(command: DomainCommand, ticket: Option[TrackingTicket]) {
    ticket foreach { t => context.reportOperationState(InProcess(t)) }
    getHandlerByType(command.getClass).fold(
      fail => {
        context.reportProblem(fail)
        ticket match {
          case Some(t) => context.reportOperationState(NotExecuted(t, fail))
          case None => ()
        }
      },
      handler => handler.handle(command, repositories, context, ticket))
  }

  def receive: Receive = {
    case commandEnvelope: CommandEnvelope => executeCommand(commandEnvelope.command, commandEnvelope.ticket)
    case AddCommandHandlerCmd(handler) => addHandler(handler)
    case RemoveCommandHandlerCmd(commandType) => removeHandlerByType(commandType)
    case GetCommandHandlerQry(commandType) => sender ! CommandHandlerRsp(getHandlerByType(commandType))
  }
}

class CommandExecutorActorHull(val actor: ActorRef, context: AlmhirtContext) extends CommandExecutor {
  private implicit val executionContext = context.system.futureDispatcher
  def addHandler(handler: HandlesCommand) { actor ! AddCommandHandlerCmd(handler) }
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) { actor ! RemoveCommandHandlerCmd(commandType) }
  def getHandlerByType(commandType: Class[_ <: DomainCommand])(implicit atMost: Duration): AlmFuture[HandlesCommand] =
    (actor ? GetCommandHandlerQry(commandType))(atMost).mapTo[CommandHandlerRsp].map(_.handler)
  def executeCommand(commandEnvelope: CommandEnvelope) { actor ! commandEnvelope }
}