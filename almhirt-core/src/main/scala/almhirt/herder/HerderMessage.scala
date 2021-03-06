package almhirt.herder

import java.time.LocalDateTime
import akka.actor.ActorRef
import almhirt.common._
import almhirt.akkax.{ CircuitControl, CircuitState, ComponentControl, ComponentId, ComponentState }
import almhirt.problem.ProblemCause
import almhirt.tracking.CommandRepresentation

object HerderMessages {
  sealed trait HerderNotificicationMessage
  
  case object OnSystemShutdown

  object CircuitMessages {
    sealed trait CircuitMessage

    final case class RegisterCircuitControl(id: ComponentId, circuitBreaker: CircuitControl) extends CircuitMessage with HerderNotificicationMessage
    final case class DeregisterCircuitControl(id: ComponentId) extends CircuitMessage with HerderNotificicationMessage

    final case object ReportCircuitStates extends CircuitMessage
    final case class CircuitStates(states: Seq[(ComponentId, CircuitState)]) extends CircuitMessage

    sealed trait CircuitControlMessage extends CircuitMessage
    final case class AttemptCloseCircuit(id: ComponentId) extends CircuitControlMessage
    final case class RemoveFuseFromCircuit(id: ComponentId) extends CircuitControlMessage
    final case class DestroyCircuit(id: ComponentId) extends CircuitControlMessage
    final case class CircumventCircuit(id: ComponentId) extends CircuitControlMessage
  }

  object ComponentControlMessages {
    sealed trait ComponentControlMessage
    final case class RegisterComponentControl(id: ComponentId, control: ComponentControl) extends ComponentControlMessage with HerderNotificicationMessage
    final case class DeregisterComponentControl(id: ComponentId) extends ComponentControlMessage with HerderNotificicationMessage
 
    final case object ReportComponentStates extends ComponentControlMessage
    final case class ComponentStates(states: Seq[(ComponentId, ComponentState)]) extends ComponentControlMessage
  
    final case class AttemptComponentControlCommand(id: ComponentId, command: almhirt.akkax.ActorMessages.ComponentControlCommand) extends ComponentControlMessage
     
  }

  object StatusReportMessages {
    sealed trait StatusReportMessage
    final case class RegisterStatusReporter(id: ComponentId, reporter: almhirt.herder.StatusReporter) extends StatusReportMessage with HerderNotificicationMessage
    final case class DeregisterStatusReporter(id: ComponentId) extends StatusReportMessage with HerderNotificicationMessage
 
    final case class GetStatusReportFor(componentId: ComponentId, options: almhirt.akkax.reporting.StatusReportOptions) extends StatusReportMessage
    sealed trait GetStatusReportForRsp extends StatusReportMessage
    final case class StatusReportFor(componentId: ComponentId, report: almhirt.akkax.reporting.StatusReport) extends GetStatusReportForRsp
    final case class GetStatusReportForFailed(componentId: ComponentId, problem: Problem) extends GetStatusReportForRsp
     
    case object GetStatusReporters extends StatusReportMessage 
    final case class StatusReporters(reporters: Seq[(ComponentId, StatusReporter)]) extends StatusReportMessage
    
  }
  
  object EventMessages {
    sealed trait EventsMessage

    final case class MissedEvent(id: ComponentId, event: Event, severity: almhirt.problem.Severity, reason: ProblemCause, timestamp: LocalDateTime) extends EventsMessage with HerderNotificicationMessage

    case object ReportMissedEvents extends EventsMessage
    final case class MissedEvents(missedEvents: Seq[(ComponentId, BadThingsHistory[MissedEventsEntry])]) extends EventsMessage

    final case class ReportMissedEventsFor(id: ComponentId) extends EventsMessage
    final case class ReportedMissedEventsFor(id: ComponentId, missedEvents: Option[BadThingsHistory[MissedEventsEntry]]) extends EventsMessage

  }

  object CommandMessages {
    sealed trait CommandsMessage

    final case class RejectedCommand(id: ComponentId, command: CommandRepresentation, severity: almhirt.problem.Severity, reason: ProblemCause, timestamp: LocalDateTime) extends CommandsMessage with HerderNotificicationMessage

    case object ReportRejectedCommands extends CommandsMessage
    final case class RejectedCommands(rejectedCommands: Seq[(ComponentId, BadThingsHistory[RejectedCommandsEntry])]) extends CommandsMessage

    final case class ReportRejectedCommandsFor(id: ComponentId) extends CommandsMessage
    final case class ReportedRejectedCommandsFor(id: ComponentId, rejectedCommands: Option[BadThingsHistory[RejectedCommandsEntry]]) extends CommandsMessage

  }

  object FailureMessages {
    sealed trait FailuresMessage

    final case class FailureOccured(id: ComponentId, failure: ProblemCause, severity: almhirt.problem.Severity, timestamp: LocalDateTime) extends FailuresMessage with HerderNotificicationMessage

    case object ReportFailures extends FailuresMessage
    final case class ReportedFailures(failures: Seq[(ComponentId, BadThingsHistory[FailuresEntry])]) extends FailuresMessage

    final case class ReportFailuresFor(id: ComponentId) extends FailuresMessage
    final case class ReportedFailuresFor(id: ComponentId, entry: Option[BadThingsHistory[FailuresEntry]]) extends FailuresMessage
  }

  object InformationMessages {
    sealed trait InformationMessage

    final case class Information(id: ComponentId, message: String, importance: Importance, timestamp: LocalDateTime) extends InformationMessage with HerderNotificicationMessage

    case object ReportInformation extends InformationMessage
    final case class ReportedInformation(information: Seq[(ComponentId, ImportantThingsHistory[InformationEntry])]) extends InformationMessage

    final case class ReportInformationFor(id: ComponentId) extends InformationMessage
    final case class ReportedInformationFor(id: ComponentId, entry: Option[ImportantThingsHistory[InformationEntry]]) extends InformationMessage

  }
}