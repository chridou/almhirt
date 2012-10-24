package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.messaging._
import almhirt.parts.HasCommandHandlers
import almhirt.commanding.HandlesCommand
import almhirt.NotFoundProblem
import almhirt.commanding.DomainCommand
import almhirt.commanding.ExecutesCommands
import almhirt.context.AlmhirtContext
import almhirt.parts.CommandExecutor

/**
 * Handles incoming commands. __NOT__ thread safe. Do not mutate, once almhirt is running!
 */
class UnsafeCommandExecutor(env: AlmhirtEnvironment, context: AlmhirtContext) extends CommandExecutor {
  private val handlers: collection.mutable.Map[String, HandlesCommand] = collection.mutable.HashMap.empty

  def addHandler(handler: HandlesCommand) { handlers.put(handler.commandType.getName, handler)}
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {
    handlers.remove(commandType.getName)
  }
  def getHandlerByType(commandType: Class[_ <: DomainCommand]): AlmValidation[HandlesCommand] = 
    handlers.get(commandType.getName) match {
      case Some(h) => h.success
      case None => NotFoundProblem("No handler found for command %s".format(commandType.getName)).failure
  }
    
  def executeCommand(command: DomainCommand) {
    getHandlerForCommand(command).fold(
      fail =>
        command.ticket match {
          case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, fail)))
          case None => ()
        },
      handler => handler.handle(command, env, context))
  }
}