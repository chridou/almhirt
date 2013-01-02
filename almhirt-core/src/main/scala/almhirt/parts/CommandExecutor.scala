package almhirt.parts

import almhirt.common._
import almhirt.environment._
import almhirt.commanding.ExecutesCommands

trait CommandExecutor extends ExecutesCommands with almhirt.almakka.ActorBased

object CommandExecutor {
  import scalaz.syntax.validation._
  import akka.actor._
  def apply(hasCommandHandlers: HasCommandHandlers, repositories: HasRepositories)(implicit almhirt: Almhirt): AlmValidation[CommandExecutor] = {
    val actor = almhirt.system.actorSystem.actorOf(Props(new impl.JustFireCommandExecutorActor(hasCommandHandlers, repositories)), "CommandExecutor")
    new impl.CommandExecutorActorHull(actor).success
  }
}

