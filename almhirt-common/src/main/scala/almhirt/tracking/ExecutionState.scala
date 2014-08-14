package almhirt.tracking

import almhirt.common._
import org.joda.time.LocalDateTime

sealed trait ExecutionState {
  def ticket: TrackingTicket
  def metadata: Map[String, String]
}

object ExecutionState {
  def executionStateOrderingTag(executionState: ExecutionState): Int =
    executionState match {
      case st: ExecutionStarted ⇒ 1
      case st: ExecutionInProcess ⇒ 2
      case st: ExecutionFinishedState ⇒ 3
    }

  def compareExecutionState(a: ExecutionState, b: ExecutionState): Int =
    executionStateOrderingTag(a) compareTo executionStateOrderingTag(b)
}

final case class ExecutionStarted(ticket: TrackingTicket, metadata: Map[String, String]) extends ExecutionState
object ExecutionStarted {
  def apply(ticket: TrackingTicket): ExecutionStarted =
    ExecutionStarted(ticket, Map.empty)
}

final case class ExecutionInProcess(ticket: TrackingTicket, metadata: Map[String, String]) extends ExecutionState
object ExecutionInProcess {
  def apply(ticket: TrackingTicket)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionInProcess =
    ExecutionInProcess(ticket, Map.empty)
}

sealed trait ExecutionFinishedState extends ExecutionState

final case class ExecutionSuccessful(ticket: TrackingTicket, message: String, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionSuccessful {
  def apply(ticket: TrackingTicket, message: String): ExecutionSuccessful =
    ExecutionSuccessful(ticket, message, Map.empty)
}

final case class ExecutionFailed(ticket: TrackingTicket, problem: Problem, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionFailed {
  def apply(ticket: TrackingTicket, problem: Problem): ExecutionFailed =
    ExecutionFailed(ticket, problem, Map.empty)
}

final case class ExecutionStateChanged(id: EventId, timestamp: LocalDateTime, executionState: ExecutionState) extends SystemEvent

object ExecutionStateChanged {
  def apply(executionState: ExecutionState)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStateChanged =
    ExecutionStateChanged(EventId(ccuad.getUniqueString()), ccuad.getUtcTimestamp, executionState)
}