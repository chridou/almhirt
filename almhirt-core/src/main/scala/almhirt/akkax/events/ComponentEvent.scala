package almhirt.akkax.events

import almhirt.common._
import almhirt.akkax.ComponentId
import almhirt.tracking.CommandRepresentation

trait ComponentEvent extends SystemEvent {
  def componentId: ComponentId
}

final case class FailureReported(header: EventHeader, componentId: ComponentId, failure: Problem, severity: almhirt.problem.Severity) extends ComponentEvent
object FailureReported {
  def apply(failure: Problem, severity: almhirt.problem.Severity)(header: EventHeader, componentId: ComponentId): FailureReported =
    FailureReported(header, componentId, failure, severity)
}

final case class EventNotProcessed(header: EventHeader, componentId: ComponentId, missedEventId: EventId, missedEventType: String, severity: almhirt.problem.Severity) extends ComponentEvent
object EventNotProcessed {
  def apply(missedEventId: EventId, missedEventType: String, severity: almhirt.problem.Severity)(header: EventHeader, componentId: ComponentId): EventNotProcessed =
    EventNotProcessed(header, componentId, missedEventId, missedEventType, severity)
}

final case class CommandRejected(header: EventHeader, componentId: ComponentId, command: CommandRepresentation, severity: almhirt.problem.Severity) extends ComponentEvent
object CommandRejected {
  def apply(command: CommandRepresentation, severity: almhirt.problem.Severity)(header: EventHeader, componentId: ComponentId): CommandRejected =
    CommandRejected(header, componentId, command, severity: almhirt.problem.Severity)
}