package almhirt.components

import org.joda.time.LocalDateTime
import scala.concurrent.duration._
import akka.actor._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.common._

object ExecutionStateTracker {

  sealed trait ExecutionStateTrackerMessage
  final case class GetExecutionStateFor(trackId: String) extends ExecutionStateTrackerMessage
  final case class QueriedExecutionState(trackId: String, executionState: Option[ExecutionState]) extends ExecutionStateTrackerMessage
  final case class SubscribeForFinishedState(trackId: String) extends ExecutionStateTrackerMessage
  final case class UnsubscribeForFinishedState(trackId: String) extends ExecutionStateTrackerMessage
  sealed trait ExecutionFinishedResultMessage extends ExecutionStateTrackerMessage
  final case class FinishedExecutionStateResult(result: ExecutionFinishedState) extends ExecutionFinishedResultMessage
  final case class ExecutionTrackingExpired(trackId: String) extends ExecutionFinishedResultMessage
  final case class RemoveOldExecutionStates(maxAge: org.joda.time.Duration) extends ExecutionStateTrackerMessage

  import scalaz.syntax.validation._
  import akka.pattern.ask
  import akka.util.Timeout
  import almhirt.almfuture.all._

  trait SecondLevelStore {
    def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[ExecutionStateEntry]]
  }

  import scala.concurrent.ExecutionContext

  def secondLevelStore(actor: ActorRef)(implicit executionContext: ExecutionContext): SecondLevelStore = {
    new SecondLevelStore {
      def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[ExecutionStateEntry]] =
        (actor ? ExecutionStateStore.GetEntry(trackId))(atMost).successfulAlmFuture[ExecutionStateStore.GetEntryResponse].mapV(res =>
          res match {
            case ExecutionStateStore.GetEntryResult(entry) => entry.success
            case ExecutionStateStore.GetEntryFailure(problem) => problem.failure
          })
    }
  }
}

trait ExecutionStateTracker { actor: Actor with ActorLogging =>

  def handleTrackingMessage: Receive
}


