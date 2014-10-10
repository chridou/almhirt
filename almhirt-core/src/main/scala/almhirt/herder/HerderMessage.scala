package almhirt.herder

import akka.actor.ActorRef
import almhirt.akkax.AlmCircuitBreaker

object HerderMessage {
  
  
  sealed trait CircuitBreakerMessage
  
  final case class RegisterCircuitBreaker(owner: ActorRef, circuitBreaker: AlmCircuitBreaker) extends CircuitBreakerMessage
  final case class DeregisterCircuitBreaker(owner: ActorRef) extends CircuitBreakerMessage
  
  final case object ReportCircuitBreakerStates extends CircuitBreakerMessage
  final case class CircuitBreakerStates(states: Map[String, AlmCircuitBreaker.State]) extends CircuitBreakerMessage

  sealed trait CircuitBreakerControlMessage extends CircuitBreakerMessage
  final case class ResetCircuitBreaker(name: String) extends CircuitBreakerControlMessage
  final case class RemoveFuseFromCircuitBreaker(name: String) extends CircuitBreakerControlMessage
  final case class DestroyFuseInCircuitBreaker(name: String) extends CircuitBreakerControlMessage
}