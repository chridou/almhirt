package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.messaging._
import almhirt.parts._
import almhirt.environment._
import almhirt.commanding.HandlesCommand
import almhirt.NotFoundProblem
import almhirt.commanding.DomainCommand
import almhirt.commanding.ExecutesCommands
import almhirt.environment.AlmhirtEnvironment
import almhirt.parts.CommandExecutor
import almhirt.util._

/**
 * Handles incoming commands. __NOT__ thread safe. Do not mutate, once almhirt is running!
 */
class UnsafeCommandExecutorOnCallingThread(repositories: HasRepositories, context: AlmhirtContext) extends CommandExecutor {
  private val handlers: collection.mutable.Map[String, HandlesCommand] = collection.mutable.HashMap.empty

  def addHandler(handler: HandlesCommand) {
    handlers.put(handler.commandType.getName, handler)
  }

  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {
    handlers.remove(commandType.getName)
  }

  def getHandlerByType(commandType: Class[_ <: DomainCommand]): AlmValidation[HandlesCommand] =
    handlers.get(commandType.getName) match {
      case Some(h) => h.success
      case None => NotFoundProblem("No handler found for command %s".format(commandType.getName), severity = Major).failure
    }

  def executeCommand(command: DomainCommand, ticket: Option[String]) {
    ticket foreach { t => context.reportOperationState(InProcess(t)) }
    getHandlerForCommand(command).fold(
      fail => {
        context.reportProblem(fail)
        ticket match {
          case Some(t) => context.reportOperationState(NotExecuted(t, fail))
          case None => ()
        }
      },
      handler => handler.handle(command, repositories, context, ticket))
  }
}