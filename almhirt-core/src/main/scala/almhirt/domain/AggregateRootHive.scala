package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates.AggregateRootId
import almhirt.snapshots._
import almhirt.tracking._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import almhirt.context.AlmhirtContext
import org.reactivestreams.{ Publisher }
import akka.stream.actor.{ ActorSubscriber, ActorSubscriberMessage, ZeroRequestStrategy }
import almhirt.streaming._

final case class HiveDescriptor(val value: String) extends AnyVal

object AggregateRootHive {
  def propsRaw(
    hiveDescriptor: HiveDescriptor,
    aggregateEventLogToResolve: ToResolve,
    snapshottingToResolve: Option[(ToResolve, SnapshottingPolicyProvider)],
    resolveSettings: ResolveSettings,
    commandBuffersize: Int,
    droneFactory: AggregateRootDroneFactory,
    otherThanContextEventBroker: Option[StreamBroker[Event]],
    enqueuedEventsThrottlingThreshold: Int)(implicit ctx: AlmhirtContext): Props =
    Props(new AggregateRootHive(
      hiveDescriptor,
      aggregateEventLogToResolve,
      snapshottingToResolve,
      resolveSettings,
      commandBuffersize,
      droneFactory,
      otherThanContextEventBroker.getOrElse(ctx.eventBroker),
      enqueuedEventsThrottlingThreshold))

  def props(
    hiveDescriptor: HiveDescriptor,
    droneFactory: AggregateRootDroneFactory,
    hiveConfigName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val aggregateEventLogToResolve = ResolvePath(ctx.localActorPaths.eventLogs / almhirt.eventlog.AggregateRootEventLog.actorname)
    val snapshotRepositoryToResolve = ResolvePath(ctx.localActorPaths.misc / almhirt.snapshots.SnapshotRepository.actorname)
    val path = "almhirt.components.aggregates.aggregate-root-hive" + hiveConfigName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      snapshottingToResolve ← section.magicOption[com.typesafe.config.Config]("snapshot-policy").flatMap {
        case None ⇒ scalaz.Success(None)
        case Some(cfg) ⇒
          SnapshottingPolicyProvider.snapshootAllByConfig(cfg).map(provider ⇒ Some((snapshotRepositoryToResolve, provider)))
      }
      resolveSettings ← section.v[ResolveSettings]("resolve-settings")
      commandBuffersize ← section.v[Int]("command-buffer-size")
      enqueuedEventsThrottlingThreshold ← section.v[Int]("enqueued-events-throttling-threshold")
    } yield propsRaw(
      hiveDescriptor,
      aggregateEventLogToResolve,
      snapshottingToResolve,
      resolveSettings,
      commandBuffersize,
      droneFactory,
      Some(ctx.eventBroker),
      enqueuedEventsThrottlingThreshold)
  }
}

private[almhirt] object AggregateRootHiveInternals {
  import almhirt.problem.ProblemCause

  sealed trait AggregateDroneMessage

  sealed trait AggregateDroneLoggingMessage extends AggregateDroneMessage

  final case class ReportDroneDebug(msg: String) extends AggregateDroneLoggingMessage
  final case class ReportDroneError(msg: String, cause: ProblemCause) extends AggregateDroneLoggingMessage
  final case class ReportDroneWarning(msg: String, cause: Option[ProblemCause]) extends AggregateDroneLoggingMessage

  final case class CargoJettisoned(aggId: AggregateRootId) extends AggregateDroneMessage

  sealed trait ExecuteCommandResponse extends AggregateDroneMessage {
    def command: Command
    def isSuccess: Boolean
  }
  final case class CommandExecuted(command: AggregateRootCommand) extends ExecuteCommandResponse {
    def isSuccess = true
  }
  final case class CommandNotExecuted(command: AggregateRootCommand, problem: Problem) extends ExecuteCommandResponse {
    def isSuccess = false
  }

  final case class Busy(command: AggregateRootCommand) extends ExecuteCommandResponse {
    def isSuccess = false
  }

  sealed trait OverdueMessage extends AggregateDroneMessage

  sealed trait SomethingIsOverdue extends OverdueMessage {
    def aggId: AggregateRootId
    def elapsed: FiniteDuration
  }

  sealed trait SomethingOverdueGotDone extends OverdueMessage {
    def aggId: AggregateRootId
  }

  final case class DispatchEventsToStreamTakesTooLong(aggId: AggregateRootId, elapsed: FiniteDuration) extends SomethingIsOverdue
  final case class EventsFinallyDispatchedToStream(aggId: AggregateRootId) extends SomethingOverdueGotDone

  final case class LogEventsTakesTooLong(aggId: AggregateRootId, elapsed: FiniteDuration) extends SomethingIsOverdue
  final case class EventsFinallyLogged(aggId: AggregateRootId) extends SomethingOverdueGotDone
}

private[almhirt] class AggregateRootHive(
  override val hiveDescriptor: HiveDescriptor,
  override val aggregateEventLogToResolve: ToResolve,
  override val snapshottingToResolve: Option[(ToResolve, SnapshottingPolicyProvider)],
  override val resolveSettings: ResolveSettings,
  override val commandBuffersize: Int,
  override val droneFactory: AggregateRootDroneFactory,
  override val eventsBroker: StreamBroker[Event],
  override val enqueuedEventsThrottlingThreshold: Int)(implicit override val almhirtContext: AlmhirtContext)
    extends AlmActor with AlmActorLogging with ActorContractor[Event] with ActorLogging with ActorSubscriber with ControllableActor with StatusReportingActor with AggregateRootHiveSkeleton {

  override val requestStrategy = ZeroRequestStrategy

  override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

}

private[almhirt] trait AggregateRootHiveSkeleton extends ActorContractor[Event] { me: AlmActor with AlmActorLogging with ActorLogging with ActorSubscriber with ControllableActor with StatusReportingActor ⇒
  import AggregateRootHive._
  import AggregateRootHiveInternals._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case exn: AggregateRootEventStoreFailedReadingException ⇒
        reportMajorFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Restart.")
        Restart
      case exn: RebuildAggregateRootFailedException ⇒
        reportCriticalFailure(exn)
        logError(s"Handling escalated error for ${sender.path.name} with a action Escalate.", exn)
        Escalate
      case exn: CouldNotDispatchAllAggregateRootEventsException ⇒
        reportMajorFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Resume.")
        Resume
      case exn: WrongAggregateRootEventTypeException ⇒
        reportCriticalFailure(exn)
        logError(s"Handling escalated error for ${sender.path.name} with a action Escalate.", exn)
        Escalate
      case exn: UserInitializationFailedException ⇒
        reportMajorFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Restart.")
        Restart
      case exn: Exception ⇒
        reportCriticalFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Escalate.")
        Escalate
    }

  def aggregateEventLogToResolve: ToResolve
  def snapshottingToResolve: Option[(ToResolve, SnapshottingPolicyProvider)]
  def resolveSettings: ResolveSettings

  def hiveDescriptor: HiveDescriptor
  def commandBuffersize: Int
  def droneFactory: AggregateRootDroneFactory
  implicit val futuresContext: ExecutionContext = almhirtContext.futuresContext
  def eventsBroker: StreamBroker[Event]
  def enqueuedEventsThrottlingThreshold: Int

  private var numReceivedInternal = 0
  private var numSucceededInternal = 0
  private var numFailedInternal = 0

  private var numJettisonedSinceLastReport = 0

  def numReceived = numReceivedInternal
  def numSucceeded = numSucceededInternal
  def numFailed = numFailedInternal

  private case object ReportThrottlingState

  private case object Resolve
  def receiveResolve: Receive = startup() {
    reportsStatus(onReportRequested = createStatusReport) {
      case Resolve ⇒
        val actorsToResolve =
          Map("aggregateeventlog" → aggregateEventLogToResolve) ++
            snapshottingToResolve.map(r ⇒ Map("snapshotting" → r._1)).getOrElse(Map.empty)
        context.resolveMany(actorsToResolve, resolveSettings, None, Some("resolver"))

      case ActorMessages.ManyResolved(dependencies, _) ⇒
        logInfo("Found dependencies.")
        signContract(eventsBroker)

        context.become(receiveInitialize(dependencies("aggregateeventlog"), dependencies.get("snapshotting").map((_, snapshottingToResolve.get._2))))

      case ActorMessages.ManyNotResolved(problem, _) ⇒
        logError(s"Failed to resolve dependencies:\n$problem")
        reportCriticalFailure(problem)
        sys.error(s"Failed to resolve dependencies.")
    }
  }

  def receiveInitialize(aggregateEventLog: ActorRef, snapshotting: Option[(ActorRef, SnapshottingPolicyProvider)]): Receive = startup() {
    case ReadyForDeliveries ⇒
      requestCommands()
      context.become(receiveRunning(aggregateEventLog, snapshotting, Set.empty))
  }

  private var totalSuppliesRequested = 0
  private var suppliesRequestedSinceThrottlingStateChanged = 0
  private var throttledSince = Deadline.now

  private var numberOfCommandsThatCanBeRequested: Int = commandBuffersize
  private var bufferedEvents: Vector[CommandStatusChanged] = Vector.empty

  private var throttled = false
  private def requestCommands() {
    if (!throttled) {
      if (bufferedEvents.size > enqueuedEventsThrottlingThreshold) {
        throttledSince = Deadline.now
        suppliesRequestedSinceThrottlingStateChanged = 0
        logWarning(s"""|Number of buffered events(${bufferedEvents.size}) is getting too large(>=$enqueuedEventsThrottlingThreshold). 
                       |Can not dispatch the command results fast enough. Throttling.
                       |Total requested events: $totalSuppliesRequested
                       |Requested events since throttling state changed: $suppliesRequestedSinceThrottlingStateChanged""".stripMargin)
        throttled = true
        context.system.scheduler.scheduleOnce(1.minute, self, ReportThrottlingState)
      } else if (numberOfCommandsThatCanBeRequested > 0) {
        request(numberOfCommandsThatCanBeRequested)
        numberOfCommandsThatCanBeRequested = 0
      }
    }
  }

  private def receivedCommandResponse() {
    numberOfCommandsThatCanBeRequested = numberOfCommandsThatCanBeRequested + 1
  }

  private def receivedInvalidCommand() {
    numberOfCommandsThatCanBeRequested = numberOfCommandsThatCanBeRequested + 1
  }

  private def enqueueEvent(event: CommandStatusChanged) {
    bufferedEvents = bufferedEvents :+ event
    offer(1)
  }

  private def deliverEvents(amount: Int) {
    totalSuppliesRequested += amount
    suppliesRequestedSinceThrottlingStateChanged += amount
    val toDeliver = bufferedEvents.take(amount)
    val rest = bufferedEvents.drop(toDeliver.size)
    deliver(toDeliver)
    bufferedEvents = rest
    if (throttled) {
      if (rest.isEmpty) {
        throttled = false
        logInfo("Released throttle.")
        suppliesRequestedSinceThrottlingStateChanged = 0
      } else {
        logInfo(s"There are still ${rest.size} command status event(s) that need to be delivered. Can not release the throttle.")
      }
    }
  }

  def receiveRunning(aggregateEventLog: ActorRef, snapshotting: Option[(ActorRef, SnapshottingPolicyProvider)], haveOverdueActions: Set[AggregateRootId]): Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case ActorSubscriberMessage.OnNext(aggregateCommand: AggregateRootCommand) ⇒
        numReceivedInternal += 1
        if (haveOverdueActions.isEmpty) {
          val drone = context.child(aggregateCommand.aggId.value) match {
            case Some(drone) ⇒
              drone
            case None ⇒
              droneFactory(aggregateCommand, aggregateEventLog, snapshotting) match {
                case scalaz.Success(props) ⇒
                  val droneActor = context.actorOf(props, aggregateCommand.aggId.value)
                  context watch droneActor
                  droneActor
                case scalaz.Failure(problem) ⇒ {
                  reportCriticalFailure(problem.withArg("hive", hiveDescriptor.value))
                  throw new Exception(s"Could not create a drone for command ${aggregateCommand.header}:\n$problem")
                }
              }
          }
          drone ! aggregateCommand
          enqueueEvent(CommandExecutionInitiated(aggregateCommand))
        } else {
          numFailedInternal += 1
          val msg = s"Rejecting command because there are ${haveOverdueActions.size} drones which are overdue completing their actions."
          val prob = ServiceBusyProblem(msg).withArg("hive", hiveDescriptor.value)
          reportRejectedCommand(aggregateCommand, MinorSeverity, prob)
          receivedInvalidCommand()
          enqueueEvent(CommandExecutionFailed(aggregateCommand, prob))
          requestCommands()
        }

      case ActorSubscriberMessage.OnNext(something) ⇒
        reportMinorFailure(ArgumentProblem(s"Received something I cannot handle: $something").withArg("hive", hiveDescriptor.value))
        logWarning(s"Received something I cannot handle: $something")
        receivedInvalidCommand()
        requestCommands()

      case rsp: ExecuteCommandResponse ⇒
        receivedCommandResponse()
        if (rsp.isSuccess) {
          numSucceededInternal += 1
        } else {
          numFailedInternal += 1
        }
        val event: CommandStatusChanged = rsp match {
          case CommandExecuted(command) ⇒
            CommandSuccessfullyExecuted(command)
          case CommandNotExecuted(command, problem) ⇒
            val newProb = problem.withArg("hive", hiveDescriptor.value)
            reportRejectedCommand(command, MinorSeverity, newProb)
            CommandExecutionFailed(command, newProb)
          case Busy(command) ⇒
            val msg = s"Command[${command.getClass.getSimpleName}] with id ${command.commandId.value} on aggregate root ${command.aggId.value} can not be executed since another command is being executed."
            val prob = CollisionProblem(msg).withArg("hive", hiveDescriptor.value)
            reportRejectedCommand(command, MinorSeverity, prob)
            CommandExecutionFailed(command, prob)
        }
        enqueueEvent(event)
        requestCommands()

      case AggregateRootHiveInternals.DispatchEventsToStreamTakesTooLong(aggId, elapsed) ⇒
        logWarning(s"Drone ${aggId.value} is overdue on dispatching events after ${elapsed.defaultUnitString}.")
        context.become(receiveRunning(aggregateEventLog, snapshotting, haveOverdueActions + aggId))

      case AggregateRootHiveInternals.LogEventsTakesTooLong(aggId, elapsed) ⇒
        logWarning(s"Drone ${aggId.value} is overdue on logging events after ${elapsed.defaultUnitString}.")
        context.become(receiveRunning(aggregateEventLog, snapshotting, haveOverdueActions + aggId))

      case m: AggregateRootHiveInternals.SomethingOverdueGotDone ⇒
        logInfo(s"Drone ${m.aggId.value} got it's stuff done.")
        context.become(receiveRunning(aggregateEventLog, snapshotting, haveOverdueActions - m.aggId))

      case OnDeliverSuppliesNow(amount) ⇒
        deliverEvents(amount)
        requestCommands()

      case ActorSubscriberMessage.OnComplete ⇒
        logDebug(s"Aggregate command stream completed after receiving $numReceived commands. $numSucceeded succeeded, $numFailed failed.")

      case OnBrokerProblem(problem) ⇒
        reportCriticalFailure(problem.withArg("hive", hiveDescriptor.value))
        throw new Exception(s"The broker reported a problem:\n$problem")

      case OnContractExpired ⇒
        logInfo(s"Contract with broker expired. There are ${bufferedEvents.size} events still to deliver.")

      case ReportDroneDebug(msg) ⇒
        logDebug(s"Drone ${sender().path.name} reported a debug message: $msg")

      case ReportDroneError(msg, cause) ⇒
        logError(s"Drone ${sender().path.name} reported an error: $msg")
        reportMajorFailure(cause.mapProblem { _.withArg("hive", hiveDescriptor.value) })

      case ReportDroneWarning(msg, causeOpt) ⇒
        causeOpt match {
          case None ⇒
            logWarning(s"""Drone ${sender().path.name} reported a warning with message "$msg".""")
          case Some(cause) ⇒
            logWarning(s"""Drone ${sender().path.name} reported a warning with message "$msg" and and a problem with message "${cause.message}".""")
            reportMinorFailure(cause.mapProblem { _.withArg("hive", hiveDescriptor.value) })
        }

      case AggregateRootHiveInternals.CargoJettisoned(id) ⇒
        this.numJettisonedSinceLastReport += 1

      case ReportThrottlingState ⇒
        if (throttled) {
          logInfo(s"""|I'm throttling for ${throttledSince.lap.defaultUnitString}
                    |Number of buffered events: ${bufferedEvents.size}
                    |Throttling threshold: $enqueuedEventsThrottlingThreshold 
                    |Total requested events: $totalSuppliesRequested
                    |Requested events since throttling state changed: $suppliesRequestedSinceThrottlingStateChanged""".stripMargin)
          context.system.scheduler.scheduleOnce(1.minute, self, ReportThrottlingState)
        }

      case Terminated(actor) ⇒
        logDebug(s"""${actor} terminated.""")
    }
  }

  override def receive: Receive = receiveResolve

  def createStatusReport(options: ReportOptions): AlmValidation[StatusReport] = {
    val numJet = numJettisonedSinceLastReport
    this.numJettisonedSinceLastReport = 0

    val rep = StatusReport(s"Hive-${this.hiveDescriptor.value}-Report") addMany (
      "number-of-drones" -> this.context.children.size,
      "number-of-commands-received" -> numReceived,
      "number-of-commands-succeeded" -> numSucceeded,
      "number-of-commands-failed" -> numFailed,
      "number-of-buffered-command-status-changed-events" -> bufferedEvents.size,
      "command-buffer-size" -> commandBuffersize,
      "amount-of-jettisoned-cargo-since-last-report" -> numJet,
      "throttled" -> throttled)
    scalaz.Success(rep)
  }

  override def preStart() {
    super.preStart()
    logInfo(s""" |Starting...
                 |
                 |command-buffer-size: $commandBuffersize
                 |enqueued-events-throttling-threshold: $enqueuedEventsThrottlingThreshold""".stripMargin)
    self ! Resolve
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    cancelContract()
    reportCriticalFailure(reason)
    logWarning(s"[Restart]: Received $numReceived commands. $numSucceeded succeeded, $numFailed failed.")
  }

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    registerComponentControl()
    registerStatusReporter(description = None)
    self ! Resolve
  }

  override def postStop() {
    super.postStop()
    cancelContract()
    deregisterComponentControl()
    deregisterStatusReporter()
    logWarning(s"Stopped. Received $numReceived commands. $numSucceeded succeeded, $numFailed failed.")
  }

}