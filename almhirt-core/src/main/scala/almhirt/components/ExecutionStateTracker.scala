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
  final case class SubscribeForFinishedState(trackId: String, subscribeMe: ActorRef) extends ExecutionStateTrackerMessage
  final case class UnsubscribeForFinishedState(trackId: String, unsubscribeMe: ActorRef) extends ExecutionStateTrackerMessage
  final case class FinishedExecutionStateResult(result: ExecutionFinishedState) extends ExecutionStateTrackerMessage
  final case class ExecutionTrackingExpired(trackId: String) extends ExecutionStateTrackerMessage
  final case class RemoveOldExecutionStates(maxAge: org.joda.time.Duration) extends ExecutionStateTrackerMessage
}

trait ExecutionStateTracker { actor: Actor with ActorLogging =>

  def handleTrackingMessage: Receive
}

