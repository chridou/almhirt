package almhirt.herder.herdingdogs

import akka.actor.Actor
import almhirt.context._

import almhirt.herder.HerderMessage

object CircuitBreakerHerdingDog {

  val actorname = "circuit-breaker-herdingdog"
}


private[almhirt] class CircuitBreakerHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext {

  override def receive: Receive = {
    case _ => ???
  }
} 