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

object CommandEndpoint {
  def propsRaw(
    commandStatusTrackerToResolve: ToResolve,
    nexusToResolve: Option[ToResolve],
    resolveSettings: ResolveSettings,
    maxTrackingDuration: FiniteDuration,
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props =
    Props(new CommandEndpointImpl(
      commandStatusTrackerToResolve,
      nexusToResolve,
      resolveSettings,
      maxTrackingDuration,
      autoConnect))

  def props(nexusToResolve: Option[ToResolve])(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val commandStatusTrackerToResolve = ResolvePath(ctx.localActorPaths.misc / CommandStatusTracker.actorname)
    for {
      section ← ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.command-endpoint")
      maxTrackingDuration ← section.v[FiniteDuration]("max-tracking-duration")
      resolveSettings ← section.v[ResolveSettings]("resolve-settings")
      autoConnect ← section.v[Boolean]("auto-connect")
    } yield propsRaw(commandStatusTrackerToResolve, nexusToResolve, resolveSettings, maxTrackingDuration, autoConnect)
  }

  val actorname = "command-endpoint"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.misc(root) / actorname
}

private[almhirt] class CommandEndpointImpl(
    commandStatusTrackerToResolve: ToResolve,
    nexusToResolve: Option[ToResolve],
    resolveSettings: ResolveSettings,
    maxTrackingDuration: FiniteDuration,
    autoConnect: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor {
  import CommandStatusTracker._

  override val componentControl = LocalComponentControl(self, ComponentControlActions.pauseResume, Some(logWarning))

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  implicit val executor = almhirtContext.futuresContext
  private case object Resolve

  private var commandStatusTracker: ActorRef = null
  private var nexus: Option[ActorRef] = None

  private var numCommandsReceived = 0L
  private var numCommandsReceivedWhileInactive = 0L
  private var numCommandsRejected = 0L
  private var numCommandsRejectedDueToMissingDemand = 0L
  private var numCommandsDispatched = 0L
  private var numResponsesAccepted = 0L
  private var numResponsesNotAccepted = 0L
  private var numCommandsSentToTracker = 0L
  private var lastCommandReceived: Option[java.time.LocalDateTime] = None
  private var lastCommandReceivedFrom: Option[String] = None
  private var lastCommandReceivedCommandId: Option[CommandId] = None
  private var lastCommandDispatched: Option[java.time.LocalDateTime] = None

  def receiveResolve: Receive = startup() {
    reportsStatus(onReportRequested = createStatusReport) {
      case Resolve ⇒
        val toResolve = (List("commandStatusTracker" -> commandStatusTrackerToResolve) ++ nexusToResolve.map(("nexus", _))).toMap

        context.resolveMany(toResolve, resolveSettings, None, None)

      case ActorMessages.ManyResolved(resolved, _) ⇒
        commandStatusTracker = resolved("commandStatusTracker")
        nexus = resolved.get("nexus")
        context.become(receiveRunning(commandStatusTracker))

      case ActorMessages.ManyNotResolved(problem, _) ⇒
        logError(s"Could not resolve dependencies:\n$problem")
        sys.error(s"Could not resolve dependencies.")
        reportCriticalFailure(problem)

      case cmd: Command ⇒
        numCommandsReceivedWhileInactive = numCommandsReceivedWhileInactive + 1L
        lastCommandReceived = Some(almhirtContext.getUtcTimestamp)
        lastCommandReceivedCommandId = Some(cmd.commandId)
        sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("Command endpoint not ready! Try again later."))
    }
  }

  def receiveRunning(commandStatusTracker: ActorRef): Receive = runningWithPause(receivePause(commandStatusTracker)) {
    reportsStatus(onReportRequested = createStatusReport) {

      case cmd: AggregateRootCommand ⇒
        val pinnedSender = sender()
        numCommandsReceived = numCommandsReceived + 1L
        lastCommandReceived = Some(almhirtContext.getUtcTimestamp)
        lastCommandReceivedFrom = Some(pinnedSender.path.toStringWithoutAddress)
        lastCommandReceivedCommandId = Some(cmd.commandId)
        nexus match {
          case Some(nx) ⇒ context.actorOf(Props(new SingleAggregateRootCommandDispatcher(cmd, pinnedSender, nx)))
          case None     ⇒ pinnedSender ! CommandNotAccepted(cmd.commandId, IllegalOperationProblem("There is no nexus."))
        }

      case cmd: Command ⇒
        val pinnedSender = sender()
        numCommandsReceived = numCommandsReceived + 1L
        lastCommandReceived = Some(almhirtContext.getUtcTimestamp)
        lastCommandReceivedFrom = Some(pinnedSender.path.toStringWithoutAddress)
        lastCommandReceivedCommandId = Some(cmd.commandId)
        pinnedSender ! CommandNotAccepted(cmd.commandId, OperationNotSupportedProblem("Handle a non aggregate root command."))
    }
  }

  def receivePause(commandStatusTracker: ActorRef): Receive = pause(receiveRunning(commandStatusTracker)) {
    reportsStatus(onReportRequested = createStatusReport) {
      case cmd: Command ⇒
        numCommandsReceivedWhileInactive = numCommandsReceivedWhileInactive + 1L
        lastCommandReceived = Some(almhirtContext.getUtcTimestamp)
        sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("I'm taking a break. Try again later."))
    }
  }

  override def receive: Receive = receiveResolve

  private def createStatusReport(option: StatusReportOptions): AlmValidation[StatusReport] = {
    val incoming = StatusReport() addMany (
      "last-command-received-command-id" -> lastCommandReceivedCommandId.map(_.value),
      "last-command-received" -> lastCommandReceived,
      "last-command-received-from" -> lastCommandReceivedFrom,
      "number-of-commands-received" -> numCommandsReceived,
      "number-of-commands-received-while-inactive" -> numCommandsReceivedWhileInactive,
      "number-of-commands-rejected" -> numCommandsRejected,
      "number-of-commands-rejected-due-to-missing-demand" -> numCommandsRejectedDueToMissingDemand)
    val outgoing = StatusReport() addMany (
      "last-command-dispatched" -> lastCommandDispatched,
      "number-of-accepted-responses" -> numResponsesAccepted,
      "number-of-not-accepted-responses" -> numResponsesNotAccepted,
      "number-of-commands-sent-to-tracker" -> numCommandsSentToTracker,
      "number-of-commands-dispatched" -> numCommandsDispatched)
    val rep = StatusReport("CommandEndpoint-Report").withComponentState(componentState) addMany (
      "max-tracking-duration" -> maxTrackingDuration,
      "incoming" -> incoming,
      "outgoing" -> outgoing)
    rep.success
  }

  override def preStart() {
    logInfo("Starting...")
    registerComponentControl()
    registerStatusReporter(description = Some("Internals from the command endpoint"))
    context.parent ! ActorMessages.ConsiderMeForReporting
    self ! Resolve
  }

  override def postStop() {
    deregisterComponentControl()
    deregisterStatusReporter()
    logInfo("Stopped")
  }

  private class SingleAggregateRootCommandDispatcher(command: AggregateRootCommand, stakeholder: ActorRef, nexus: ActorRef) extends Actor {
    def receive: Receive = {
      case CommandAccepted(_) ⇒
        sendCommandToTracker()
        this.context.stop(self)

      case m: CommandNotAccepted ⇒
        stakeholder ! m
        this.context.stop(self)
    }

    private def sendCommandToTracker() {
      if (command.isTrackable) {
        commandStatusTracker ! TrackCommand(
          commandId = command.commandId,
          callback = cmdRes ⇒ cmdRes.fold(
            fail ⇒ {
              val rsp = TrackingFailed(command.commandId, fail)
              stakeholder.tell(rsp, ActorRef.noSender)
              reportMinorFailure(fail)
              reportRejectedCommand(command, MinorSeverity, fail)
            },
            trackingResult ⇒ {
              val rsp = TrackedCommandResult(command.commandId, trackingResult)
              stakeholder.tell(rsp, ActorRef.noSender)
            }),

          deadline = maxTrackingDuration.fromNow)
      } else {
        stakeholder ! CommandAccepted(command.commandId)
      }
    }

    override def preStart() {
      super.preStart()
      nexus ! command
    }
  }

}