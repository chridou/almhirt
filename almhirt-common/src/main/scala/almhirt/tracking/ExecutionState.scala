package almhirt.tracking

import almhirt.common._
import org.joda.time.LocalDateTime

sealed trait ExecutionState {
  def ticket: TrackingTicket
  def metadata: Map[String, String]
  def timestamp: LocalDateTime
}

object ExecutionState {
  def executionStateOrderingTag(executionState: ExecutionState): Int =
    executionState match {
      case st: ExecutionStarted => 1
      case st: ExecutionInProcess => 2
      case st: ExecutionFinishedState => 3
    }

  def compareExecutionState(a: ExecutionState, b: ExecutionState): Int =
    if (a.isInstanceOf[ExecutionInProcess] && b.isInstanceOf[ExecutionInProcess])
      a.timestamp compareTo b.timestamp
    else
      executionStateOrderingTag(a) compareTo executionStateOrderingTag(b)
}

final case class ExecutionStarted(ticket: TrackingTicket, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionState
object ExecutionStarted {
  def apply(ticket: TrackingTicket)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStarted =
    ExecutionStarted(ticket, ccuad.getUtcTimestamp, Map.empty)
  def apply(ticket: TrackingTicket, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStarted =
    ExecutionStarted(ticket, ccuad.getUtcTimestamp, metadata)
}

final case class ExecutionInProcess(ticket: TrackingTicket, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionState
object ExecutionInProcess {
  def apply(ticket: TrackingTicket)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionInProcess =
    ExecutionInProcess(ticket, ccuad.getUtcTimestamp, Map.empty)
  def apply(ticket: TrackingTicket, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionInProcess =
    ExecutionInProcess(ticket, ccuad.getUtcTimestamp, metadata)
}

sealed trait ExecutionFinishedState extends ExecutionState

final case class ExecutionSuccessful(ticket: TrackingTicket, message: String, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionSuccessful {
  def apply(ticket: TrackingTicket, message: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionSuccessful =
    ExecutionSuccessful(ticket, message, ccuad.getUtcTimestamp, Map.empty)
  def apply(ticket: TrackingTicket, message: String, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionSuccessful =
    ExecutionSuccessful(ticket, message, ccuad.getUtcTimestamp, metadata)
}

final case class ExecutionFailed(ticket: TrackingTicket, problem: Problem, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionFailed {
  def apply(ticket: TrackingTicket, problem: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionFailed =
    ExecutionFailed(ticket, problem, ccuad.getUtcTimestamp, Map.empty)
  def apply(ticket: TrackingTicket, problem: Problem, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionFailed =
    ExecutionFailed(ticket, problem, ccuad.getUtcTimestamp, metadata)
}

final case class ExecutionStateChanged(id: EventId, executionState: ExecutionState) extends SystemEvent

object ExecutionStateChanged {
  def apply(executionState: ExecutionState)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStateChanged =
    ExecutionStateChanged(EventId(ccuad.getUniqueString()), executionState)
}