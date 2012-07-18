package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._

trait AlmAkka {
  def defaultActorSystem = AlmAkka.actorSystem
  implicit def defaultFutureDispatch = AlmAkka.defaultFutureDispatch
  implicit def defaultTimeout = AlmAkka.defaultTimeout
}

object AlmAkka {
  val actorSystem = ActorSystem("almhirt")
  val defaultFutureDispatch: MessageDispatcher = actorSystem.dispatchers.lookup("almhirt.almhirt-async")

  val shortTimeout = 1 seconds
  val defaultTimeout = 3 seconds
  val longTimeout = 5 seconds
  
}
