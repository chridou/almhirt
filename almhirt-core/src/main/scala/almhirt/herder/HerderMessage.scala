package almhirt.herder

import org.joda.time.LocalDateTime
import akka.actor.ActorRef
import almhirt.common._
import almhirt.akkax.{ CircuitControl, CircuitState, ComponentId }
import almhirt.problem.ProblemCause

object HerderMessages {
  sealed trait HerderNotificicationMessage

  object CircuitMessages {
    sealed trait CircuitMessage

    final case class RegisterCircuitControl(id: ComponentId, circuitBreaker: CircuitControl) extends CircuitMessage with HerderNotificicationMessage
    final case class DeregisterCircuitControl(id: ComponentId) extends CircuitMessage with HerderNotificicationMessage

    final case object ReportCircuitStates extends CircuitMessage
    final case class CircuitStates(states: Seq[(ComponentId, CircuitState)]) extends CircuitMessage

    sealed trait CircuitControlMessage extends CircuitMessage
    final case class AttemptCloseCircuit(id: ComponentId) extends CircuitControlMessage
    final case class RemoveFuseFromCircuit(id: ComponentId) extends CircuitControlMessage
    final case class DestroyFuseInCircuit(id: ComponentId) extends CircuitControlMessage
  }

  object EventMessages {
    sealed trait EventsMessage

    final case class MissedEvent(id: ComponentId, event: Event, severity: almhirt.problem.Severity, problem: Problem, timestamp: LocalDateTime) extends EventsMessage with HerderNotificicationMessage

    case object ReportMissedEvents extends EventsMessage
    final case class MissedEvents(missed: Seq[(ComponentId, almhirt.problem.Severity, Int)]) extends EventsMessage
  }

  object FailureMessages {
    sealed trait FailuresMessage

    final case class FailureOccured(id: ComponentId, failure: ProblemCause, severity: almhirt.problem.Severity, timestamp: LocalDateTime) extends FailuresMessage with HerderNotificicationMessage

    case object ReportFailures extends FailuresMessage
    final case class ReportedFailures(entries: Seq[(ComponentId, FailuresEntry)]) extends FailuresMessage

    final case class ReportFailuresFor(id: ComponentId) extends FailuresMessage 
    final case class ReportedFailuresFor(id: ComponentId, entry: Option[FailuresEntry]) extends FailuresMessage
  }

}