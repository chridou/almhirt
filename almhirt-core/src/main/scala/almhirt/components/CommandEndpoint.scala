package almhirt.components

import scala.concurrent.duration._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.tracking._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import almhirt.context.HasAlmhirtContext
import akka.stream.actor._
import akka.stream.scaladsl._

object CommandEndpoint {
  def propsRaw(
    commandStatusTrackerToResolve: ToResolve,
    resolveSettings: ResolveSettings,
    maxTrackingDuration: FiniteDuration,
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props =
    Props(new CommandEndpointImpl(
      commandStatusTrackerToResolve,
      resolveSettings,
      maxTrackingDuration,
      autoConnect))

  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val commandStatusTrackerToResolve = ResolvePath(ctx.localActorPaths.misc / CommandStatusTracker.actorname)
    for {
      section ← ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.command-endpoint")
      maxTrackingDuration ← section.v[FiniteDuration]("max-tracking-duration")
      resolveSettings ← section.v[ResolveSettings]("resolve-settings")
      autoConnect ← section.v[Boolean]("auto-connect")
    } yield propsRaw(commandStatusTrackerToResolve, resolveSettings, maxTrackingDuration, autoConnect)
  }

  def apply(commandEndpoint: ActorRef): org.reactivestreams.Publisher[Command] =
    ActorPublisher[Command](commandEndpoint)

  val actorname = "command-endpoint"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.misc(root) / actorname
}

private[almhirt] class CommandEndpointImpl(
    commandStatusTrackerToResolve: ToResolve,
    resolveSettings: ResolveSettings,
    maxTrackingDuration: FiniteDuration,
    autoConnect: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends ActorPublisher[Command] with AlmActor with AlmActorLogging with ActorLogging with ControllableActor with ControllableActorWithPauseResume {
  import CommandStatusTracker._

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  private case object AutoConnect
  private case object Resolve

  def receiveResolve: Receive = {
    case Resolve ⇒
      registerComponentControl()
      context.resolveSingle(commandStatusTrackerToResolve, resolveSettings, None, Some("status-tracker-resolver"))

    case ActorMessages.ResolvedSingle(commandStatusTracker, _) ⇒
      logInfo("Found command status tracker.")
      if (autoConnect) self ! AutoConnect
      context.become(receiveRunning(commandStatusTracker))

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      logError(s"Could not resolve command status tracker @ ${commandStatusTrackerToResolve}:\n$problem")
      sys.error(s"Could not resolve command status tracker log @ ${commandStatusTrackerToResolve}.")
      reportCriticalFailure(problem)

    case cmd: Command ⇒
      sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("Command endpoint not ready! Try again later."))

  }

  def receiveRunning(commandStatusTracker: ActorRef): Receive = {
    case AutoConnect ⇒
      logInfo("Connecting to command consumer.")
      CommandEndpoint(self).subscribe(almhirtContext.commandBroker.newSubscriber)

    case cmd: Command ⇒
      dispatchCommandResult(
        cmd,
        checkCommandDispatchable(cmd),
        sender(),
        commandStatusTracker)

    case m: ActorMessages.ComponentControlMessage ⇒
      runningControlHandler(receivePause(commandStatusTracker))(m)
  }

  def receivePause(commandStatusTracker: ActorRef): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒
      pauseControlHandler(receiveRunning(commandStatusTracker))(m)

    case cmd: Command ⇒
      sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("I'm taking a break. Try again later."))
  }

  override def receive: Receive = receiveResolve orElse (startupTerminator)

  private def checkCommandDispatchable(cmd: Command): AlmValidation[Command] =
    if (totalDemand > 0 && isActive) {
      cmd.success
    } else {
      val reason =
        if (isCanceled) {
          ServiceShutDownProblem("Command processing was shut down. The stream subscriber cancelled the stream.")
        } else if (isErrorEmitted) {
          ServiceBrokenProblem("Command processing is broken. An error was already emitted.")
        } else if (!isActive) {
          ServiceNotAvailableProblem("Command processing is not yet ready.")
        } else if (isCompleted) {
          ServiceNotAvailableProblem("The service is not available any more. The commad stream was closed.")
        } else if (totalDemand == 0) {
          ServiceBusyProblem("Currently there is no demand for commands. Try again later.")
        } else {
          UnspecifiedProblem("Unknown cause.")
        }
      reason.failure
    }

  private def dispatchCommandResult(cmd: Command, result: AlmValidation[Command], receiver: ActorRef, commandStatusTracker: ActorRef) {
    result.fold(
      problem ⇒ {
        logWarning(s"""|Rejecting command with id "${cmd.commandId.value}".
        					|Current demand is $totalDemand commands.
        					|Reason:
        					|$problem""".stripMargin)
        receiver ! CommandNotAccepted(cmd.commandId, problem)
        reportMinorFailure(problem)
        reportRejectedCommand(cmd, MinorSeverity, problem)
      },
      dispatchableCmd ⇒ {
        if (dispatchableCmd.isTrackable) {
          commandStatusTracker ! TrackCommand(
            commandId = dispatchableCmd.commandId,
            callback = _.fold(
              fail ⇒ {
                fail match {
                  case OperationTimedOutProblem(p) ⇒ receiver ! TrackingFailed(dispatchableCmd.commandId, p)
                  case _                           ⇒ receiver ! TrackingFailed(dispatchableCmd.commandId, fail)
                }
                reportMinorFailure(fail)
                reportRejectedCommand(cmd, MinorSeverity, fail)
              },
              receiver ! TrackedCommandResult(dispatchableCmd.commandId, _)),
            deadline = maxTrackingDuration.fromNow)
        } else {
          receiver ! CommandAccepted(dispatchableCmd.commandId)
        }
        onNext(dispatchableCmd)
      })
  }

  override def preStart() {
    logInfo("Starting...")
    self ! Resolve
  }

  override def postStop() {
    deregisterComponentControl()
    logInfo("Stopped")
  }

}