package almhirt.parts

import almhirt.common._
import almhirt.environment._
import almhirt.commanding.ExecutesCommands

trait CommandExecutor extends HasCommandHandlers with ExecutesCommands with almhirt.core.ActorBased

object CommandExecutor {
  import scalaz.syntax.validation._
  import akka.actor._
  def apply(repositories: HasRepositories)(implicit context: AlmhirtContext): AlmValidation[CommandExecutor] = {
    val actor = context.system.actorSystem.actorOf(Props(new impl.JustFireCommandExecutorActor(repositories)), "CommandExecutor")
    new impl.CommandExecutorActorHull(actor, context).success
  }
}

