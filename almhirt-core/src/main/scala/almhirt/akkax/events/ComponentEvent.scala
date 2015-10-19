package almhirt.akkax.events

import almhirt.common._
import almhirt.akkax.GlobalComponentId
import almhirt.tracking.CommandRepresentation

trait ComponentEvent extends SystemEvent {
  def origin: GlobalComponentId
}

final case class FailureReported(header: EventHeader, origin: GlobalComponentId, failure: Problem, severity: almhirt.problem.Severity) extends ComponentEvent
object FailureReported {
  def apply(failure: Problem, severity: almhirt.problem.Severity)(header: EventHeader, origin: GlobalComponentId): FailureReported =
    FailureReported(header, origin, failure, severity)
}

final case class EventNotProcessed(header: EventHeader, origin: GlobalComponentId, missedEventId: EventId, missedEventType: String, severity: almhirt.problem.Severity) extends ComponentEvent
object EventNotProcessed {
  def apply(missedEventId: EventId, missedEventType: String, severity: almhirt.problem.Severity)(header: EventHeader, origin: GlobalComponentId): EventNotProcessed =
    EventNotProcessed(header, origin, missedEventId, missedEventType, severity)
}

final case class CommandRejected(header: EventHeader, origin: GlobalComponentId, command: CommandRepresentation, severity: almhirt.problem.Severity) extends ComponentEvent
object CommandRejected {
  def apply(command: CommandRepresentation, severity: almhirt.problem.Severity)(header: EventHeader, origin: GlobalComponentId): CommandRejected =
    CommandRejected(header, origin, command, severity: almhirt.problem.Severity)
}

final case class SystemStarted(header: EventHeader, origin: GlobalComponentId) extends ComponentEvent
final case class SystemStopped(header: EventHeader, origin: GlobalComponentId) extends ComponentEvent