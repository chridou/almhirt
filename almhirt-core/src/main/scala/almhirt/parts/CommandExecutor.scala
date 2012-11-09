package almhirt.parts

import almhirt.environment._
import almhirt.commanding.ExecutesCommands

trait CommandExecutor extends HasCommandHandlers with ExecutesCommands with almhirt.ActorBased

object CommandExecutor {
  import akka.actor._
  def apply(repositories: HasRepositories)(implicit context: AlmhirtContext): CommandExecutor = {
    val actor = context.system.actorSystem.actorOf(Props(new impl.JustFireCommandExecutorActor(repositories)), "CommandExecutor")
    new impl.CommandExecutorActorHull(actor, context)
  }
}

