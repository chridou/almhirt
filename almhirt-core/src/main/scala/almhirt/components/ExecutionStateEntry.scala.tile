package almhirt.components

import org.joda.time.LocalDateTime
import almhirt.common.CanCreateDateTime
import almhirt.core.types._

final case class ExecutionStateEntry(currentState: ExecutionState, lastModified: LocalDateTime) {
  def isFinished: Boolean = currentState match {
    case _: ExecutionFinishedState => true
    case _ => false
  }

  def tryGetFinished: Option[ExecutionFinishedState] =
    currentState match {
      case f: ExecutionFinishedState => Some(f)
      case _ => None
    }
}

object ExecutionStateEntry {
  def apply(currentState: ExecutionState)(implicit ccuad: CanCreateDateTime): ExecutionStateEntry =
    ExecutionStateEntry(currentState, ccuad.getUtcTimestamp)
}
