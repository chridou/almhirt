package almhirt.components

//Misc
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.tracking.CommandStatus
// Streaming
import akka.stream.actor._

object CommandEndpoint {
  sealed trait CommandEndpointMessage

  sealed trait CommandResponse extends CommandEndpointMessage
  final case class CommandAccepted(id: CommandId) extends CommandResponse
  final case class CommandNotAccepted(id: CommandId) extends CommandResponse

  sealed trait TrackedCommandResponse extends CommandResponse
  final case class TrackedCommandResult(id: CommandId, status: CommandStatus) extends TrackedCommandResponse
  final case class TrackedCommandTimedOut(id: CommandId) extends TrackedCommandResponse
  final case class TrackerFailed(id: CommandId, problem: Problem) extends TrackedCommandResponse

  def props(commandStatusTracker: ActorRef, maxTrackingDuration: FiniteDuration): Props =
    Props(new CommandEndpointImpl(commandStatusTracker, maxTrackingDuration))

  def apply(commandEndpoint: ActorRef): org.reactivestreams.Publisher[Command] =
    ActorPublisher[Command](commandEndpoint)
}

private[almhirt] class CommandEndpointImpl(commandStatusTracker: ActorRef, maxTrackingDuration: FiniteDuration) extends ActorPublisher[Command] with ActorLogging {
  import CommandEndpoint._
  import CommandStatusTracker._

  def receiveRunning: Receive = {
    case cmd: Command =>
      if (totalDemand > 0 && isActive) {
        if (cmd.isTrackable) {
          val pinnedSender = sender()
          commandStatusTracker ! TrackCommand(
            commandId = cmd.commandId,
            callback = _.fold(
              fail =>
                fail match {
                  case OperationTimedOutProblem(_) => pinnedSender ! TrackedCommandTimedOut(cmd.commandId)
                  case _ => pinnedSender ! TrackerFailed(cmd.commandId, fail)
                },
              pinnedSender ! TrackedCommandResult(cmd.commandId, _)),
            deadline = maxTrackingDuration.fromNow)
        } else {
          sender() ! CommandAccepted(cmd.commandId)
        }
        onNext(cmd)
      } else {
        sender() ! CommandNotAccepted(cmd.commandId)
      }
  }

  override def receive: Receive = receiveRunning

}