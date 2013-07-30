package almhirt.components

import org.joda.time.LocalDateTime
import scala.concurrent.duration._
import akka.actor._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.common._

object ExecutionStateTracker {
  final case class TrackingEntry(currentState: ExecutionState, lastModified: LocalDateTime) {
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

  object TrackingEntry {
    def apply(currentState: ExecutionState)(implicit ccuad: CanCreateDateTime): TrackingEntry =
      TrackingEntry(currentState, ccuad.getUtcTimestamp)
  }

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

object ExecutionStateStore {
  import scala.concurrent.ExecutionContext
  import ExecutionStateTracker._

  sealed trait ExecutionStateStoreMessage
  sealed trait StoreEntryResponse
  final case class StoreEntry(entry: TrackingEntry) extends ExecutionStateStoreMessage
  final case class StoreEntryState(problem: Option[Problem]) extends StoreEntryResponse
  sealed trait GetEntryResponse extends ExecutionStateStoreMessage
  final case class GetEntry(trackId: String) extends ExecutionStateStoreMessage
  final case class GetEntryResult(entry: Option[TrackingEntry]) extends GetEntryResponse
  final case class GetEntryFailure(problem: Problem) extends GetEntryResponse

  import scalaz.syntax.validation._
  import akka.pattern.ask
  import akka.util.Timeout
  import almhirt.almfuture.all._

  trait SecondLevelStoreWrapper {
    def store(entry: TrackingEntry)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit]
    def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[TrackingEntry]]
  }

  def secondLevelStoreWrapper(actor: ActorRef)(implicit executionContext: ExecutionContext): SecondLevelStoreWrapper = {
    new SecondLevelStoreWrapper {
      def store(entry: TrackingEntry)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit] =
        (actor ? StoreEntry(entry))(atMost).successfulAlmFuture[StoreEntryResponse].mapV(res =>
          res match {
            case StoreEntryState(None) => ().success
            case StoreEntryState(Some(problem)) => problem.failure
          })

      def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[TrackingEntry]] =
        (actor ? GetEntry(trackId))(atMost).successfulAlmFuture[GetEntryResponse].mapV(res =>
          res match {
            case GetEntryResult(entry) => entry.success
            case GetEntryFailure(problem) => problem.failure
          })
    }
  }
}


