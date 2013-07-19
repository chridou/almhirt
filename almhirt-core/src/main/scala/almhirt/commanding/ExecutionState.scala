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

trait ExecutionFinishedState extends ExecutionState

final case class ExecutionSuccessful(trackId: String, message: String, metaData: Map[String, String]) extends ExecutionFinishedState
object ExecutionSuccessful {
  def apply(trackId: String, message: String)(implicit ccuad: CanCreateUuidsAndDateTimes): ExecutionSuccessful =
    ExecutionSuccessful(trackId, message, Map("timestamp" -> ccuad.getDateTime.toString()))
}

case class ExecutionFailed(trackId: String, problem: Problem, metaData: Map[String, String]) extends ExecutionFinishedState
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