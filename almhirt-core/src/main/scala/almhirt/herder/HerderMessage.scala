package almhirt.herder

import org.joda.time.LocalDateTime
import akka.actor.ActorRef
import almhirt.common._
import almhirt.akkax.{ CircuitControl, CircuitState }

object HerderMessage {
  sealed trait HerderInputMessage
  
  sealed trait CircuitMessage
  
  final case class RegisterCircuitControl(name: String, circuitBreaker: CircuitControl) extends CircuitMessage with HerderInputMessage
  final case class DeregisterCircuitControl(name: String) extends CircuitMessage with HerderInputMessage
  
  final case object ReportCircuitStates extends CircuitMessage with HerderInputMessage
  final case class CircuitStates(states: Map[String, CircuitState]) extends CircuitMessage

  sealed trait CircuitControlMessage extends CircuitMessage with HerderInputMessage
  final case class AttemptCloseCircuit(name: String) extends CircuitControlMessage
  final case class RemoveFuseFromCircuit(name: String) extends CircuitControlMessage
  final case class DestroyFuseInCircuit(name: String) extends CircuitControlMessage
  
  
  sealed trait EventsMessage
  final case class MissedEvent(name: String, event: Event, severity: almhirt.problem.Severity, problem: Problem, timestamp: LocalDateTime) extends EventsMessage with HerderInputMessage
 
  case object ReportMissedEvents extends EventsMessage with HerderInputMessage
  final case class MissedEvents(missed: Map[String, (almhirt.problem.Severity, Int)]) extends EventsMessage
}