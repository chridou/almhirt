package almhirt.herder

import akka.actor.ActorRef
import almhirt.akkax.AlmCircuitBreaker

object HerderMessage {
  final case class RegisterCircuitBreaker(owner: ActorRef, circuitBreaker: AlmCircuitBreaker)
  final case class DeregisterCircuitBreaker(owner: ActorRef)
}