package almhirt.parts

import almhirt.common._
import almhirt.environment._
import almhirt.commanding.ExecutesCommands

trait CommandExecutor extends ExecutesCommands with almhirt.almakka.ActorBased

object CommandExecutor {
  import scalaz.syntax.validation._
  import akka.actor._
  def apply(hasCommandHandlers: HasCommandHandlers, repositories: HasRepositories)(implicit context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[CommandExecutor] = {
    val actor = system.actorSystem.actorOf(Props(new impl.JustFireCommandExecutorActor(hasCommandHandlers, repositories)), "CommandExecutor")
    new impl.CommandExecutorActorHull(actor, context).success
  }
}

