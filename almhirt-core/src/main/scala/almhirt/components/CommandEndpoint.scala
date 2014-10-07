package almhirt.components

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.tracking._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import akka.stream.actor._
import akka.stream.scaladsl2._

object CommandEndpoint {
  def propsRaw(commandStatusTrackerToResolve: ToResolve, resolveSettings: ResolveSettings, maxTrackingDuration: FiniteDuration, autoConnect: Boolean)(implicit ctx: AlmhirtContext): Props =
    Props(new CommandEndpointImpl(commandStatusTrackerToResolve, resolveSettings, maxTrackingDuration, autoConnect))

  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      section <- ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.command-endpoint")
      commandStatusTrackerPathStr <- section.v[String]("command-status-tracker-path")
      commandStatusTrackerToResolve <- inTryCatch { ResolvePath(ActorPath.fromString(commandStatusTrackerPathStr)) }
      maxTrackingDuration <- section.v[FiniteDuration]("max-tracking-duration")
      resolveSettings <- section.v[ResolveSettings]("resolve-settings")
      autoConnect <- section.v[Boolean]("auto-connect")
    } yield propsRaw(commandStatusTrackerToResolve, resolveSettings, maxTrackingDuration, autoConnect)
  }

  def apply(commandEndpoint: ActorRef): org.reactivestreams.Publisher[Command] =
    ActorPublisher[Command](commandEndpoint)

  val actorname = "command-endpoint"
}

private[almhirt] class CommandEndpointImpl(
  commandStatusTrackerToResolve: ToResolve,
  resolveSettings: ResolveSettings,
  maxTrackingDuration: FiniteDuration,
  autoConnect: Boolean)(implicit ctx: AlmhirtContext) extends ActorPublisher[Command] with ActorLogging with ImplicitFlowMaterializer {
  import CommandStatusTracker._

  private case object AutoConnect
  private case object Resolve
  def receiveResolve: Receive = {
    case Resolve ⇒
      context.resolveSingle(commandStatusTrackerToResolve, resolveSettings, None, Some("status-tracker-resolver"))

    case ActorMessages.ResolvedSingle(commandStatusTracker, _) ⇒
      log.info("Found command status tracker.")
      if (autoConnect) self ! AutoConnect
      context.become(receiveRunning(commandStatusTracker))

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      log.error(s"Could not resolve command status tracker @ ${commandStatusTrackerToResolve}:\n$problem")
      sys.error(s"Could not resolve command status tracker log @ ${commandStatusTrackerToResolve}.")

    case cmd: Command ⇒
      sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("Command endpoint not ready! Try again later."))
  }

  def receiveRunning(commandStatusTracker: ActorRef): Receive = {
    case AutoConnect ⇒
      log.info("Connecting to command consumer.")
      CommandEndpoint(self).subscribe(ctx.commandBroker.newSubscriber)

    case cmd: Command ⇒
      if (totalDemand > 0 && isActive) {
        if (cmd.isTrackable) {
          val pinnedSender = sender()
          commandStatusTracker ! TrackCommand(
            commandId = cmd.commandId,
            callback = _.fold(
              fail ⇒
                fail match {
                  case OperationTimedOutProblem(p) ⇒ pinnedSender ! TrackingFailed(cmd.commandId, p)
                  case _ ⇒ pinnedSender ! TrackingFailed(cmd.commandId, fail)
                },
              pinnedSender ! TrackedCommandResult(cmd.commandId, _)),
            deadline = maxTrackingDuration.fromNow)
        } else {
          sender() ! CommandAccepted(cmd.commandId)
        }
        onNext(cmd)
      } else {
        val reason =
          if (!isCanceled) {
            ServiceShutDownProblem("Command processing was shut down.")
          } else if (isErrorEmitted) {
            ServiceBrokenProblem("Command processing is broken.")
          } else if (!isActive) {
            ServiceNotAvailableProblem("Command processing is not yet ready.")
          } else if (totalDemand == 0) {
            ServiceBusyProblem("No demand. Try again later.")
          } else {
            UnspecifiedProblem("Unknown cause.")
          }
        sender() ! CommandNotAccepted(cmd.commandId, reason)
      }
  }

  override def receive: Receive = receiveResolve

  override def preStart() {
    self ! Resolve
  }

}