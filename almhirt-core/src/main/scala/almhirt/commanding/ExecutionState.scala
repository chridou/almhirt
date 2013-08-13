package almhirt.commanding

import almhirt.common._
import org.joda.time.LocalDateTime

sealed trait ExecutionState {
  def trackId: String
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

final case class ExecutionStarted(trackId: String, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionState
object ExecutionStarted {
  def apply(trackId: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStarted =
    ExecutionStarted(trackId, ccuad.getUtcTimestamp, Map.empty)
  def apply(trackId: String, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStarted =
    ExecutionStarted(trackId, ccuad.getUtcTimestamp, metadata)
}

final case class ExecutionInProcess(trackId: String, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionState
object ExecutionInProcess {
  def apply(trackId: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionInProcess =
    ExecutionInProcess(trackId, ccuad.getUtcTimestamp, Map.empty)
  def apply(trackId: String, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionInProcess =
    ExecutionInProcess(trackId, ccuad.getUtcTimestamp, metadata)
}

trait ExecutionFinishedState extends ExecutionState

final case class ExecutionSuccessful(trackId: String, message: String, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionSuccessful {
  def apply(trackId: String, message: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionSuccessful =
    ExecutionSuccessful(trackId, message, ccuad.getUtcTimestamp, Map.empty)
  def apply(trackId: String, message: String, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionSuccessful =
    ExecutionSuccessful(trackId, message, ccuad.getUtcTimestamp, metadata)
}

case class ExecutionFailed(trackId: String, problem: Problem, timestamp: LocalDateTime, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionFailed {
  def apply(trackId: String, problem: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionFailed =
    ExecutionFailed(trackId, problem, ccuad.getUtcTimestamp, Map.empty)
  def apply(trackId: String, problem: Problem, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionFailed =
    ExecutionFailed(trackId, problem, ccuad.getUtcTimestamp, metadata)
}

final case class ExecutionStateChanged(header: EventHeader, executionState: ExecutionState) extends Event {
  override def changeMetadata(newMetadata: Map[String, String]): ExecutionStateChanged = copy(header = this.header.changeMetadata(newMetadata))
}

object ExecutionStateChanged {
  def apply(executionState: ExecutionState)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStateChanged =
    ExecutionStateChanged(EventHeader(), executionState)
}