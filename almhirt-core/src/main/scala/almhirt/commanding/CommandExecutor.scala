package almhirt.commanding

import akka.actor._

trait CommandExecutor { actor: Actor with ActorLogging =>
  def receiveCommandExecutorMessage: Receive
}

