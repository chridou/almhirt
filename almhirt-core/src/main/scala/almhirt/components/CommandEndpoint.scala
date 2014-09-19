package almhirt.components

//Misc
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.tracking._
// Streaming
import akka.stream.actor._

object CommandEndpoint {
  def props(commandStatusTracker: ActorRef, maxTrackingDuration: FiniteDuration): Props =
    Props(new CommandEndpointImpl(commandStatusTracker, maxTrackingDuration))

  def apply(commandEndpoint: ActorRef): org.reactivestreams.Publisher[Command] =
    ActorPublisher[Command](commandEndpoint)
}

private[almhirt] class CommandEndpointImpl(commandStatusTracker: ActorRef, maxTrackingDuration: FiniteDuration) extends ActorPublisher[Command] with ActorLogging {
  import CommandStatusTracker._

  def receiveRunning: Receive = {
    case cmd: Command ⇒
      if (totalDemand > 0 && isActive) {
        if (cmd.isTrackable) {
          val pinnedSender = sender()
          commandStatusTracker ! TrackCommand(
            commandId = cmd.commandId,
            callback = _.fold(
              fail ⇒
                fail match {
                  case OperationTimedOutProblem(_) ⇒ pinnedSender ! TrackedCommandTimedOut(cmd.commandId)
                  case _ ⇒ pinnedSender ! TrackerFailed(cmd.commandId, fail)
                },
              pinnedSender ! TrackedCommandResult(cmd.commandId, _)),
            deadline = maxTrackingDuration.fromNow)
        } else {
          sender() ! CommandAccepted(cmd.commandId)
        }
        onNext(cmd)
      } else {
        val msg =
          if (!isCanceled) {
            "Command processing was shut down."
          } else if (isErrorEmitted) {
            "Command processing is broken."
          } else if (!isActive) {
            "Command processing is not yet ready."
          } else if (totalDemand == 0) {
            "No demand. Try again later."
          } else {
            "No reason."
          }
        sender() ! CommandNotAccepted(cmd.commandId, msg)
      }
  }

  override def receive: Receive = receiveRunning

}