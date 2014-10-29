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
import akka.stream.scaladsl2._

object CommandEndpoint {
  def propsRaw(
    commandStatusTrackerToResolve: ToResolve,
    resolveSettings: ResolveSettings,
    maxTrackingDuration: FiniteDuration,
    circuitControlSettings: CircuitControlSettings,
    circuitStateReportingInterval: Option[FiniteDuration],
    circuitControlCallbackExecutor: ExtendedExecutionContextSelector,
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props =
    Props(new CommandEndpointImpl(
      commandStatusTrackerToResolve,
      resolveSettings,
      maxTrackingDuration,
      circuitControlSettings,
      circuitStateReportingInterval,
      circuitControlCallbackExecutor,
      autoConnect))

  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      section ← ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.command-endpoint")
      commandStatusTrackerPathStr ← section.v[String]("command-status-tracker-path")
      commandStatusTrackerToResolve ← inTryCatch { ResolvePath(ActorPath.fromString(commandStatusTrackerPathStr)) }
      maxTrackingDuration ← section.v[FiniteDuration]("max-tracking-duration")
      resolveSettings ← section.v[ResolveSettings]("resolve-settings")
      circuitControlSettings ← section.v[CircuitControlSettings]("circuit-control")
      circuitControlCallbackExecutor ← section.v[ExtendedExecutionContextSelector]("circuit-control.callback-executor")
      circuitStateReportingInterval ← section.magicOption[FiniteDuration]("circuit-state-reporting-interval")
      autoConnect ← section.v[Boolean]("auto-connect")
    } yield propsRaw(commandStatusTrackerToResolve, resolveSettings, maxTrackingDuration, circuitControlSettings, circuitStateReportingInterval, circuitControlCallbackExecutor, autoConnect)
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
  override val circuitControlSettings: CircuitControlSettings,
  override val circuitStateReportingInterval: Option[FiniteDuration],
  override val circuitControlCallbackExecutorSelector: ExtendedExecutionContextSelector,
  autoConnect: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends ActorPublisher[Command] with AlmActor with AlmActorLogging with SyncFusedActor with ActorLogging with ImplicitFlowMaterializer {
  import CommandStatusTracker._

  override def circuitControlLoggingAdapter = Some(this.log)
  override val sendStateChangedEvents = true

  private case object AutoConnect
  private case object Resolve

  def receiveResolve: Receive = {
    case Resolve ⇒
      context.resolveSingle(commandStatusTrackerToResolve, resolveSettings, None, Some("status-tracker-resolver"))

    case ActorMessages.ResolvedSingle(commandStatusTracker, _) ⇒
      logInfo("Found command status tracker.")
      if (autoConnect) self ! AutoConnect
      registerCircuitControl()
      context.becomeFused(receiveRunningWithClosedCircuit(commandStatusTracker))

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      logError(s"Could not resolve command status tracker @ ${commandStatusTrackerToResolve}:\n$problem")
      sys.error(s"Could not resolve command status tracker log @ ${commandStatusTrackerToResolve}.")
      reportCriticalFailure(problem)

    case cmd: Command ⇒
      sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("Command endpoint not ready! Try again later."))
  }

  def receiveRunningWithClosedCircuit(commandStatusTracker: ActorRef): Receive = {
    case AutoConnect ⇒
      logInfo("Connecting to command consumer.")
      CommandEndpoint(self).subscribe(almhirtContext.commandBroker.newSubscriber)

    case cmd: Command ⇒
      dispatchCommandResult(
        cmd,
        fused(checkCommandDispatchable(cmd)),
        sender(),
        commandStatusTracker)

    case m: ActorMessages.CircuitAllWillFail ⇒
      logWarning("The circuit was opened!")
      context.becomeFused(receiveRunningWithOpenCircuit(commandStatusTracker))
  }

  def receiveRunningWithOpenCircuit(commandStatusTracker: ActorRef): Receive = {
    case cmd: Command ⇒
      sender() ! CommandNotAccepted(cmd.commandId, ServiceBrokenProblem("Command processing is currently broken. Try again later."))

    case m: ActorMessages.CircuitNotAllWillFail ⇒
      logInfo("The circuit was closed!")
      context.becomeFused(receiveRunningWithClosedCircuit(commandStatusTracker))

  }

  override def receive: Receive = receiveResolve

  private def checkCommandDispatchable(cmd: Command): AlmValidation[Command] =
    if (totalDemand > 0 && isActive) {
      cmd.success
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
      reason.failure
    }

  private def dispatchCommandResult(cmd: Command, result: AlmValidation[Command], receiver: ActorRef, commandStatusTracker: ActorRef) {
    result.fold(
      problem ⇒ {
        logWarning(s"""	|
        					|Rejecting command with id "${cmd.commandId.value}".
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
                  case _ ⇒ receiver ! TrackingFailed(dispatchableCmd.commandId, fail)
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
    self ! Resolve
  }

  override def postStop() {
    deregisterCircuitControl()
  }

}