package almhirt.commanding

import almhirt.common._

sealed trait ExecutionState {
  def trackId: String
  def metadata: Map[String, String]
}

final case class ExecutionStarted(trackId: String, metadata: Map[String, String]) extends ExecutionState
object ExecutionStarted {
  def apply(trackId: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStarted =
    ExecutionStarted(trackId, Map("timestamp" -> ccuad.getDateTime.toString()))
}

final case class ExecutionInProcess(trackId: String, metadata: Map[String, String]) extends ExecutionState
object ExecutionInProcess {
  def apply(trackId: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionInProcess =
    ExecutionInProcess(trackId, Map("timestamp" -> ccuad.getDateTime.toString()))
}

trait ExecutionFinishedState extends ExecutionState

final case class ExecutionSuccessful(trackId: String, message: String, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionSuccessful {
  def apply(trackId: String, message: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionSuccessful =
    ExecutionSuccessful(trackId, message, Map("timestamp" -> ccuad.getDateTime.toString()))
}

case class ExecutionFailed(trackId: String, problem: Problem, metadata: Map[String, String]) extends ExecutionFinishedState
object ExecutionFailed {
  def apply(trackId: String, problem: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionFailed =
    ExecutionFailed(trackId, problem, Map("timestamp" -> ccuad.getDateTime.toString()))
}

final case class ExecutionStateChanged(header: EventHeader, executionState: ExecutionState) extends Event {
   override def changeMetadata(newMetadata: Map[String, String]): ExecutionStateChanged = copy(header = this.header.changeMetadata(newMetadata))
}

object ExecutionStateChanged {
  def apply(executionState: ExecutionState)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionStateChanged =
    ExecutionStateChanged(EventHeader(), executionState)
}