package almhirt.components

import almhirt.common._
import almhirt.commanding.ExecutionFinishedState
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.messaging.MessagePublisher
import scala.concurrent.ExecutionContext

trait CommandEndpoint {
  def execute(command: Command): Unit
  def executeTracked(command: Command): String
  def executeSync(command: Command, atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[ExecutionFinishedState]
}

object CommandEndpoint {
  def apply(publishTo: MessagePublisher, tracker: ActorRef)(implicit ccuad: CanCreateUuidsAndDateTimes, execContext: ExecutionContext): CommandEndpoint =
    new CommandEndpointImpl(publishTo, tracker)
}

class CommandEndpointImpl(publishTo: MessagePublisher, tracker: ActorRef)(implicit ccuad: CanCreateUuidsAndDateTimes, execContext: ExecutionContext) extends CommandEndpoint {
  override def execute(command: Command) {
    publishTo.publish(command)
  }

  def executeTracked(command: Command): String = {
    val cmd =
      if (command.canBeTracked)
        command
      else
        command.track
    publishTo.publish(cmd)
    cmd.trackingId
  }

  override def executeSync(command: Command, atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[ExecutionFinishedState] = {
    import scalaz.syntax.validation._
    import almhirt.almfuture.all._
    import almhirt.components.ExecutionStateTracker._
    val cmd =
      if (command.canBeTracked)
        command
      else
        command.track
    publishTo.publish(cmd)
    (tracker ? SubscribeForFinishedState(cmd.trackingId))(atMost).successfulAlmFuture[ExecutionFinishedResultMessage].mapV(res =>
      res match {
        case FinishedExecutionStateResult(res) => res.success
        case ExecutionTrackingExpired(trackId) => UnspecifiedProblem(s"""Execution state tracking for "$trackId" has expired.""").failure
      })
  }

}