package almhirt.herder

import org.joda.time.LocalDateTime
import akka.actor.ActorRef
import almhirt.common._
import almhirt.akkax.{ CircuitControl, CircuitState, ComponentId }
import almhirt.problem.ProblemCause

object HerderMessage {
  sealed trait HerderInputMessage
  
  
  sealed trait CircuitMessage
  
  final case class RegisterCircuitControl(id: ComponentId, circuitBreaker: CircuitControl) extends CircuitMessage with HerderInputMessage
  final case class DeregisterCircuitControl(id: ComponentId) extends CircuitMessage with HerderInputMessage
  
  final case object ReportCircuitStates extends CircuitMessage with HerderInputMessage
  final case class CircuitStates(states: Map[ComponentId, CircuitState]) extends CircuitMessage

  sealed trait CircuitControlMessage extends CircuitMessage with HerderInputMessage
  final case class AttemptCloseCircuit(id: ComponentId) extends CircuitControlMessage
  final case class RemoveFuseFromCircuit(id: ComponentId) extends CircuitControlMessage
  final case class DestroyFuseInCircuit(id: ComponentId) extends CircuitControlMessage
  
  
  sealed trait EventsMessage
  
  final case class MissedEvent(id: ComponentId, event: Event, severity: almhirt.problem.Severity, problem: Problem, timestamp: LocalDateTime) extends EventsMessage with HerderInputMessage
 
  case object ReportMissedEvents extends EventsMessage with HerderInputMessage
  final case class MissedEvents(missed: Seq[(ComponentId, almhirt.problem.Severity, Int)]) extends EventsMessage
  
  
  sealed trait FailuresMessage
  
  final case class FailureOccured(id: ComponentId, failure: ProblemCause, severity: almhirt.problem.Severity, timestamp: LocalDateTime) extends FailuresMessage with HerderInputMessage

  case object ReportFailures extends FailuresMessage with HerderInputMessage
  final case class ReportedFailures(entries: Seq[(ComponentId, FailuresEntry)]) extends FailuresMessage
  
  final case class ReportFailuresFor(id: ComponentId) extends FailuresMessage with HerderInputMessage
  final case class ReportedFailuresFor(id: ComponentId, entry: Option[FailuresEntry]) extends FailuresMessage

}