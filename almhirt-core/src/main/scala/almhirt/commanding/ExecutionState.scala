package almhirt.commanding

import almhirt.common._

sealed trait ExecutionState {
  def trackId: String
  def metaData: Map[String, String]
}

final case class ExecutionStarted(trackId: String, metaData: Map[String, String]) extends ExecutionState
object ExecutionStartet {
  def apply(trackId: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStarted =
    ExecutionStarted(trackId, Map("timestamp" -> ccuad.getDateTime.toString()))
}

final case class ExecutionInProcess(trackId: String, metaData: Map[String, String]) extends ExecutionState
object ExecutionInProcess {
  def apply(trackId: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionInProcess =
    ExecutionInProcess(trackId, Map("timestamp" -> ccuad.getDateTime.toString()))
}

final case class ExecutionSuccessful(trackId: String, message: String, metaData: Map[String, String]) extends ExecutionState
object ExecutionSuccessful {
  def apply(trackId: String, message: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionSuccessful =
    ExecutionSuccessful(trackId, message, Map("timestamp" -> ccuad.getDateTime.toString()))
}

case class ExecutionFailed(trackId: String, problem: Problem, metaData: Map[String, String])
object ExecutionFailed {
  def apply(trackId: String, problem: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionFailed =
    ExecutionFailed(trackId, problem, Map("timestamp" -> ccuad.getDateTime.toString()))
}

final case class ExecutionStateEvent(header: EventHeader, executionState: ExecutionState) extends Event with EventTemplate[ExecutionStateEvent] {
  override protected def changeHeader(newHeader: EventHeader): ExecutionStateEvent =
    copy(header = newHeader)
}

object ExecutionStateEvent {
  def apply(executionState: ExecutionState)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStateEvent =
    ExecutionStateEvent(EventHeader(), executionState)
}