package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._

trait AlmAkka {
  def defaultActorSystem = AlmAkka.actorSystem
  implicit def defaultFutureDispatch = AlmAkka.defaultFutureDispatch
  implicit def defaultTimeoutDuration = AlmAkka.defaultTimeoutDuration
}

object AlmAkka {
  val actorSystem = ActorSystem("almhirt")
  val defaultFutureDispatch: MessageDispatcher = actorSystem.dispatchers.lookup("almhirt.almhirt-async")

  val shortTimeoutDuration = 1 seconds
  val defaultTimeoutDuration = 3 seconds
  val longTimeoutDuration = 5 seconds
  
}
