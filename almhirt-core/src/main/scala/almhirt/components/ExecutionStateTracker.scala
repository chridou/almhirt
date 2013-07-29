package almhirt.components

import org.joda.time.LocalDateTime
import scala.concurrent.duration._
import akka.actor._
import almhirt.core.Almhirt
import almhirt.commanding._

object ExecutionStateTracker {
  sealed trait ExecutionStateTrackerMessage

  final case class GetExecutionStateFor(trackId: String) extends ExecutionStateTrackerMessage
  final case class CurrentExecutionState(trackId: String, executionState: Option[ExecutionState]) extends ExecutionStateTrackerMessage
  final case class RegisterForFinishedState(trackId: String, registerMe: ActorRef, maxWaitTime: FiniteDuration) extends ExecutionStateTrackerMessage
  final case class FinishedExecutionStateResult(trackId: String, result: ExecutionFinishedState) extends ExecutionStateTrackerMessage
  final case class ExecutionDidNotFinishWithinTime(trackId: String, result: ExecutionFinishedState) extends ExecutionStateTrackerMessage
}

trait ExecutionStateTracker { actor: Actor with ActorLogging =>

  def handleTrackingMessage: Receive
}

