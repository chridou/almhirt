package almhirt.herder

import akka.actor.ActorRef
import almhirt.akkax.{ CircuitControl, CircuitState }

object HerderMessage {
  
  
  sealed trait CircuitMessage
  
  final case class RegisterCircuitControl(owner: ActorRef, circuitBreaker: CircuitControl) extends CircuitMessage
  final case class DeregisterCircuitControl(owner: ActorRef) extends CircuitMessage
  
  final case object ReportCircuitStates extends CircuitMessage
  final case class CircuitStates(states: Map[String, CircuitState]) extends CircuitMessage

  sealed trait CircuitControlMessage extends CircuitMessage
  final case class AttemptCloseCircuit(name: String) extends CircuitControlMessage
  final case class RemoveFuseFromCircuit(name: String) extends CircuitControlMessage
  final case class DestroyFuseInCircuit(name: String) extends CircuitControlMessage
}