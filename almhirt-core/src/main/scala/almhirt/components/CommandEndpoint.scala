package almhirt.components

import scala.concurrent.duration._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.tracking._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
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
    autoConnect: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends ActorPublisher[Command] with AlmActor with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor {
  import CommandStatusTracker._

  override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.pauseResume, Some(logWarning))

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  implicit val executor = almhirtContext.futuresContext
  private case object AutoConnect
  private case object Resolve

  private var numCommandsReceived = 0L
  private var numCommandsReceivedWhileInactive = 0L
  private var numCommandsRejected = 0L
  private var numCommandsRejectedDueToMissingDemand = 0L
  private var numCommandsDispatched = 0L
  private var numResponsesAccepted = 0L
  private var numResponsesNotAccepted = 0L
  private var numCommandsSentToTracker = 0L

  def receiveResolve: Receive = startup() {
    reportsStatus(onReportRequested = createStatusReport) {
      case Resolve ⇒
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
        numCommandsReceivedWhileInactive = numCommandsReceivedWhileInactive + 1L
        sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("Command endpoint not ready! Try again later."))

    }
  }

  def receiveRunning(commandStatusTracker: ActorRef): Receive = runningWithPause(receivePause(commandStatusTracker)) {
    reportsStatus(onReportRequested = createStatusReport) {

      case AutoConnect ⇒
        logInfo("Connecting to command consumer.")
        CommandEndpoint(self).subscribe(almhirtContext.commandBroker.newSubscriber)

      case cmd: Command ⇒
        numCommandsReceived = numCommandsReceived + 1L
        dispatchCommandResult(
          cmd,
          checkCommandDispatchable(cmd),
          sender(),
          commandStatusTracker)
    }
  }

  def receivePause(commandStatusTracker: ActorRef): Receive = pause(receiveRunning(commandStatusTracker)) {
    reportsStatus(onReportRequested = createStatusReport) {
      case cmd: Command ⇒
        numCommandsReceivedWhileInactive = numCommandsReceivedWhileInactive + 1L
        sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("I'm taking a break. Try again later."))
    }
  }

  override def receive: Receive = receiveResolve

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
          numCommandsRejectedDueToMissingDemand = numCommandsRejectedDueToMissingDemand + 1L
          ServiceBusyProblem("Currently there is no demand for commands. Try again later.")
        } else {
          UnspecifiedProblem("Unknown cause.")
        }
      numCommandsRejected = numCommandsRejected + 1L
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
        numResponsesNotAccepted = numResponsesNotAccepted + 1L
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
          numCommandsSentToTracker = numCommandsSentToTracker + 1L
        } else {
          numResponsesAccepted = numResponsesAccepted + 1L
          receiver ! CommandAccepted(dispatchableCmd.commandId)
        }
        numCommandsDispatched = numCommandsDispatched + 1L
        onNext(dispatchableCmd)
      })
  }

  private def createStatusReport(option: StatusReportOptions): AlmValidation[StatusReport] = {
    val incoming = StatusReport() addMany (
      "number-of-commands-received" -> numCommandsReceived,
      "number-of-commands-received-while-inactive" -> numCommandsReceivedWhileInactive,
      "number-of-commands-rejected" -> numCommandsRejected,
      "number-of-commands-rejected-due-to-missing-demand" -> numCommandsRejectedDueToMissingDemand)
    val outgoing = StatusReport() addMany (
      "number-of-accepted-responses" -> numResponsesAccepted,
      "number-of-not-accepted-responses" -> numResponsesNotAccepted,
      "number-of-commands-sent-to-tracker" -> numCommandsSentToTracker,
      "number-of-commands-dispatched" -> numCommandsDispatched)
    val rep = StatusReport("CommandEndpoint-Report").withComponentState(componentState) addMany (
      "current-command-demand" -> totalDemand,
      "max-tracking-duration" -> maxTrackingDuration,
      "incoming" -> incoming,
      "outgoing" -> outgoing)
    rep.success
  }

  override def preStart() {
    logInfo("Starting...")
    registerComponentControl()
    registerStatusReporter(description = Some("Internals from the command endpoint"))
    self ! Resolve
  }

  override def postStop() {
    deregisterComponentControl()
    deregisterStatusReporter()
    logInfo("Stopped")
  }

}