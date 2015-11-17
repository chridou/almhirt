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
import almhirt.streaming._

final case class HiveDescriptor(val value: String) extends AnyVal

object AggregateRootHive {
  def propsRaw(
    hiveDescriptor: HiveDescriptor,
    aggregateEventLogToResolve: ToResolve,
    snapshottingToResolve: Option[(ToResolve, SnapshottingPolicyProvider)],
    resolveSettings: ResolveSettings,
    maxParallelism: Int,
    droneFactory: AggregateRootDroneFactory,
    otherThanContextEventBroker: Option[StreamBroker[Event]],
    enqueuedEventsThrottlingThreshold: Int)(implicit ctx: AlmhirtContext): Props =
    Props(new AggregateRootHive(
      hiveDescriptor,
      aggregateEventLogToResolve,
      snapshottingToResolve,
      resolveSettings,
      maxParallelism,
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
      maxParallelism ← section.v[Int]("max-parallelism")
      enqueuedEventsThrottlingThreshold ← section.v[Int]("enqueued-events-throttling-threshold")
    } yield propsRaw(
      hiveDescriptor,
      aggregateEventLogToResolve,
      snapshottingToResolve,
      resolveSettings,
      maxParallelism,
      droneFactory,
      Some(ctx.eventBroker),
      enqueuedEventsThrottlingThreshold)
  }
}

private[almhirt] object AggregateRootHiveInternals {
  import almhirt.problem.ProblemCause

  sealed trait AggregateDroneMessage

  sealed trait AggregateDroneLoggingMessage extends AggregateDroneMessage

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
  override val maxParallelism: Int,
  override val droneFactory: AggregateRootDroneFactory,
  override val eventsBroker: StreamBroker[Event],
  override val enqueuedEventsThrottlingThreshold: Int)(implicit override val almhirtContext: AlmhirtContext)
    extends AlmActor with AlmActorLogging with ActorContractor[Event] with ActorLogging with ControllableActor with StatusReportingActor with AggregateRootHiveSkeleton {

  override val componentControl = LocalComponentControl(self, ComponentControlActions.none, Some(logWarning))

}

private[almhirt] trait AggregateRootHiveSkeleton extends ActorContractor[Event] { me: AlmActor with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor ⇒
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
        logError(s"Handling escalated error for ${sender.path.name} with a action Stop.", exn)
        //context.become(receiveErrorState(exn))
        Restart
      // Stop
      //Escalate
      case exn: CouldNotDispatchAllAggregateRootEventsException ⇒
        reportMajorFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Stop.")
        context.become(receiveErrorState(exn))
        Stop
      case exn: WrongAggregateRootEventTypeException ⇒
        reportCriticalFailure(exn)
        logError(s"Handling escalated error for ${sender.path.name} with a action Stop.", exn)
        context.become(receiveErrorState(exn))
        Stop
      //Escalate
      case exn: UserInitializationFailedException ⇒
        reportMajorFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Stop.")
        Stop
      case exn: PreStoreActionFailedException ⇒
        reportMajorFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Restart.")
        Restart
      case exn: Exception ⇒
        reportCriticalFailure(exn)
        informVeryImportant(s"Handling escalated error from ${sender.path.name} with a action Stop.")
        context.become(receiveErrorState(exn))
        Stop
      //Escalate
    }

  def aggregateEventLogToResolve: ToResolve
  def snapshottingToResolve: Option[(ToResolve, SnapshottingPolicyProvider)]
  def resolveSettings: ResolveSettings

  def hiveDescriptor: HiveDescriptor
  def maxParallelism: Int
  def droneFactory: AggregateRootDroneFactory
  implicit val futuresContext: ExecutionContext = almhirtContext.futuresContext
  def eventsBroker: StreamBroker[Event]
  def enqueuedEventsThrottlingThreshold: Int

  private var numCommandsReceivedInternal = 0
  private var numCommandsSucceededInternal = 0
  private var numCommandsFailedInternal = 0

  private var numJettisonedSinceLastReport = 0

  def numCommandsReceived = numCommandsReceivedInternal
  def numCommandsSucceeded = numCommandsSucceededInternal
  def numCommandsFailed = numCommandsFailedInternal

  private var aggregateEventLog: Option[ActorRef] = None
  private var snapshotting: Option[(ActorRef, SnapshottingPolicyProvider)] = None

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

        aggregateEventLog = Some(dependencies("aggregateeventlog"))
        snapshotting = dependencies.get("snapshotting").map((_, snapshottingToResolve.get._2))

        context.become(receiveInitialize)

      case ActorMessages.ManyNotResolved(problem, _) ⇒
        logError(s"Failed to resolve dependencies:\n$problem")
        reportCriticalFailure(problem)
        sys.error(s"Failed to resolve dependencies.")
    }
  }

  def receiveInitialize: Receive = startup() {
    case ReadyForDeliveries ⇒
      throttleIfNeccessary()
      context.become(receiveRunning)
  }

  private var totalSuppliesRequested = 0
  private var suppliesRequestedSinceThrottlingStateChanged = 0

  private var numCommandsInFlight = 0
  private var bufferedCommandStatusEventsToDispatch: Vector[CommandStatusChanged] = Vector.empty
  private var overdueActions: Set[AggregateRootId] = Set.empty

  private var throttled = false
  private var throttlingSince: Option[java.time.LocalDateTime] = None

  private var lastCommandReceivedOn: Option[java.time.LocalDateTime] = None

  private var lastCommandStatusEventsDeliveredOn: Option[java.time.LocalDateTime] = None
  private var lastCommandStatusEventsOfferedOn: Option[java.time.LocalDateTime] = None
  private var numCommandStatusEventsOffered = 0L
  private var numCommandStatusEventsDelivered = 0L

  private def throttleIfNeccessary() {
    if (!throttled) {
      if (bufferedCommandStatusEventsToDispatch.size > enqueuedEventsThrottlingThreshold) {
        throttlingSince = Some(almhirtContext.getUtcTimestamp)
        suppliesRequestedSinceThrottlingStateChanged = 0
        logWarning(s"""|Number of buffered events(${bufferedCommandStatusEventsToDispatch.size}) is getting too large(>=$enqueuedEventsThrottlingThreshold). 
                       |Can not dispatch the command results fast enough. Throttling.
                       |Total requested commands: $totalSuppliesRequested
                       |Requested commands since throttling state changed: $suppliesRequestedSinceThrottlingStateChanged""".stripMargin)
        throttled = true
        context.system.scheduler.scheduleOnce(1.minute, self, ReportThrottlingState)
      }
      //      else if (numberOfCommandsThatCanBeRequested > 0) {
      //        request(numberOfCommandsThatCanBeRequested)
      //        numberOfCommandsRequestedFromUpstream = numberOfCommandsRequestedFromUpstream + numberOfCommandsThatCanBeRequested
      //        numberOfCommandsThatCanBeRequested = 0
      //      }
    }
  }

  private def enqueueEvent(event: CommandStatusChanged) {
    bufferedCommandStatusEventsToDispatch = bufferedCommandStatusEventsToDispatch :+ event
    lastCommandStatusEventsOfferedOn = Some(almhirtContext.getUtcTimestamp)
    numCommandStatusEventsOffered = numCommandStatusEventsOffered + 1L
    offer(1)
  }

  private def deliverEvents(amount: Int) {
    totalSuppliesRequested += amount
    suppliesRequestedSinceThrottlingStateChanged += amount
    val toDeliver = bufferedCommandStatusEventsToDispatch.take(amount)
    val rest = bufferedCommandStatusEventsToDispatch.drop(toDeliver.size)
    deliver(toDeliver)
    lastCommandStatusEventsDeliveredOn = Some(almhirtContext.getUtcTimestamp)
    numCommandStatusEventsDelivered = numCommandStatusEventsDelivered + amount
    bufferedCommandStatusEventsToDispatch = rest
    if (throttled) {
      if (rest.isEmpty) {
        throttled = false
        throttlingSince = None
        logInfo("Released throttle.")
        suppliesRequestedSinceThrottlingStateChanged = 0
      } else {
        logInfo(s"There are still ${rest.size} command status event(s) that need to be delivered. Can not release the throttle.")
      }
    }
  }

  def receiveRunning: Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case aggregateCommand: AggregateRootCommand ⇒
        numCommandsReceivedInternal += 1
        lastCommandReceivedOn = Some(almhirtContext.getUtcTimestamp)
        if (throttled) {
          val prob = ServiceBusyProblem(s"I throttling since i cannot dispatch my command status events fast enough.")
          reportRejectedCommand(aggregateCommand, MinorSeverity, prob)
          sender() ! CommandNotAccepted(aggregateCommand.commandId, prob)
        } else if (numCommandsInFlight >= maxParallelism) {
          val drone = context.child(aggregateCommand.aggId.value) match {
            case Some(drone) ⇒
              drone
            case None ⇒
              aggregateEventLog match {
                case Some(aggEventLog) ⇒
                  droneFactory(aggregateCommand, aggEventLog, snapshotting) match {
                    case scalaz.Success(props) ⇒
                      val droneActor = context.actorOf(props, aggregateCommand.aggId.value)
                      context watch droneActor
                      droneActor
                    case scalaz.Failure(problem) ⇒ {
                      val prob = ServiceBrokenProblem(s"Could not create a drone for command ${aggregateCommand.header}.")
                      reportRejectedCommand(aggregateCommand, CriticalSeverity, prob)
                      reportCriticalFailure(problem.withArg("hive", hiveDescriptor.value))
                      sender() ! CommandNotAccepted(aggregateCommand.commandId, prob)
                      throw new Exception(s"Could not create a drone for command ${aggregateCommand.header}:\n$problem")
                    }
                  }
                case None ⇒
                  val prob = ServiceBrokenProblem("There is no aggregate root event log!")
                  reportRejectedCommand(aggregateCommand, CriticalSeverity, prob)
                  sender() ! CommandNotAccepted(aggregateCommand.commandId, prob)
                  val exn = new Exception(s"There is no aggregate root event log!")
                  reportCriticalFailure(exn)
                  throw exn
              }
          }
          drone ! aggregateCommand
          numCommandsInFlight = numCommandsInFlight + 1
          sender() ! CommandAccepted(aggregateCommand.commandId)
          enqueueEvent(CommandExecutionInitiated(aggregateCommand))
        } else {
          val prob = ServiceBusyProblem(s"Too many commands are running($numCommandsInFlight of max $maxParallelism).")
          reportRejectedCommand(aggregateCommand, MinorSeverity, prob)
          sender() ! CommandNotAccepted(aggregateCommand.commandId, prob)
        }

      case rsp: ExecuteCommandResponse ⇒
        numCommandsInFlight = numCommandsInFlight - 1
        if (rsp.isSuccess) {
          numCommandsSucceededInternal += 1
        } else {
          numCommandsFailedInternal += 1
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
        throttleIfNeccessary()

      case AggregateRootHiveInternals.DispatchEventsToStreamTakesTooLong(aggId, elapsed) ⇒
        logWarning(s"Drone ${aggId.value} is overdue on dispatching events after ${elapsed.defaultUnitString}.")
        overdueActions = overdueActions + aggId

      case AggregateRootHiveInternals.LogEventsTakesTooLong(aggId, elapsed) ⇒
        logWarning(s"Drone ${aggId.value} is overdue on logging events after ${elapsed.defaultUnitString}.")
        overdueActions = overdueActions + aggId

      case m: AggregateRootHiveInternals.SomethingOverdueGotDone ⇒
        logInfo(s"Drone ${m.aggId.value} got it's stuff done.")
        overdueActions = overdueActions - m.aggId

      case OnDeliverSuppliesNow(amount) ⇒
        deliverEvents(amount)
        throttleIfNeccessary()

      case OnBrokerProblem(problem) ⇒
        reportCriticalFailure(problem.withArg("hive", hiveDescriptor.value))
        throw new Exception(s"The broker reported a problem:\n$problem")

      case OnContractExpired ⇒
        logInfo(s"Contract with broker expired. There are ${bufferedCommandStatusEventsToDispatch.size} events still to deliver.")

      case ReportDroneError(msg, cause) ⇒
        logError(s"Drone ${sender().path.name} reported an error: $msg")
        reportMajorFailure(cause.mapProblem { _.withArg("hive", hiveDescriptor.value) })

      case ReportDroneWarning(msg, causeOpt) ⇒
        causeOpt match {
          case None ⇒
            logWarning(s"""Drone ${sender().path.name} reported a warning with message "$msg".""")
          case Some(cause) ⇒
            logWarning(s"""Drone ${sender().path.name} reported a warning with message "$msg" and and a problem with message "${cause.message}".""")
        }

      case AggregateRootHiveInternals.CargoJettisoned(id) ⇒
        this.numJettisonedSinceLastReport += 1

      case ReportThrottlingState ⇒
        if (throttled) {
          logInfo(s"""|I'm throttling for ${throttlingSince}
                    |Number of buffered events: ${bufferedCommandStatusEventsToDispatch.size}
                    |Throttling threshold: $enqueuedEventsThrottlingThreshold 
                    |Total requested events: $totalSuppliesRequested
                    |Requested events since throttling state changed: $suppliesRequestedSinceThrottlingStateChanged""".stripMargin)
          context.system.scheduler.scheduleOnce(1.minute, self, ReportThrottlingState)
        }

      case Terminated(actor) ⇒
        overdueActions = overdueActions - AggregateRootId(actor.path.name)
        logDebug(s"""${actor} terminated.""")
    }
  }

  private def receiveErrorState(cause: almhirt.problem.ProblemCause): Receive = error(cause) {
    reportsStatus(onReportRequested = createStatusReport) {
      case aggregateCommand: AggregateRootCommand ⇒
        numCommandsReceivedInternal += 1
        numCommandsFailedInternal += 1
        lastCommandReceivedOn = Some(almhirtContext.getUtcTimestamp)
        val msg = s"Rejecting command because I am in error state."
        val prob = ServiceBrokenProblem(msg, cause = Some(cause)).withArg("hive", hiveDescriptor.value)
        reportRejectedCommand(aggregateCommand, MajorSeverity, prob)
        enqueueEvent(CommandExecutionFailed(aggregateCommand, prob))
        throttleIfNeccessary()
        sender() ! CommandNotAccepted(aggregateCommand.commandId, ServiceBrokenProblem("I'm in error state!", cause = Some(cause)))

      case rsp: ExecuteCommandResponse ⇒
        numCommandsInFlight = numCommandsInFlight - 1
        if (rsp.isSuccess) {
          numCommandsSucceededInternal += 1
        } else {
          numCommandsFailedInternal += 1
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
        throttleIfNeccessary()

      case AggregateRootHiveInternals.DispatchEventsToStreamTakesTooLong(aggId, elapsed) ⇒
        logWarning(s"Drone ${aggId.value} is overdue on dispatching events after ${elapsed.defaultUnitString}.")
        overdueActions = overdueActions + aggId

      case AggregateRootHiveInternals.LogEventsTakesTooLong(aggId, elapsed) ⇒
        logWarning(s"Drone ${aggId.value} is overdue on logging events after ${elapsed.defaultUnitString}.")
        overdueActions = overdueActions + aggId

      case m: AggregateRootHiveInternals.SomethingOverdueGotDone ⇒
        logInfo(s"Drone ${m.aggId.value} got it's stuff done.")
        overdueActions = overdueActions - m.aggId

      case OnDeliverSuppliesNow(amount) ⇒
        deliverEvents(amount)
        throttleIfNeccessary()

      case OnBrokerProblem(problem) ⇒
        reportCriticalFailure(problem.withArg("hive", hiveDescriptor.value))
        throw new Exception(s"The broker reported a problem:\n$problem")

      case OnContractExpired ⇒
        logInfo(s"Contract with broker expired. There are ${bufferedCommandStatusEventsToDispatch.size} events still to deliver.")

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
          logInfo(s"""|I'm throttling for ${throttlingSince}
                    |Number of buffered events: ${bufferedCommandStatusEventsToDispatch.size}
                    |Throttling threshold: $enqueuedEventsThrottlingThreshold 
                    |Total requested events: $totalSuppliesRequested
                    |Requested events since throttling state changed: $suppliesRequestedSinceThrottlingStateChanged""".stripMargin)
          context.system.scheduler.scheduleOnce(1.minute, self, ReportThrottlingState)
        }

      case Terminated(actor) ⇒
        overdueActions = overdueActions - AggregateRootId(actor.path.name)
        logDebug(s"""${actor} terminated.""")
    }
  }

  override def receive: Receive = receiveResolve

  def createStatusReport(options: StatusReportOptions): AlmValidation[StatusReport] = {
    val numJet = numJettisonedSinceLastReport
    this.numJettisonedSinceLastReport = 0

    val overdueAggIds = ezreps.ast.EzField("overdue-agg-ids", traversableToEzCollection(overdueActions.toTraversable))

    val rep = StatusReport(s"Hive-${this.hiveDescriptor.value}-Report").withComponentState(componentState).subReport("command-stats",
      "number-of-commands-in-flight" -> numCommandsInFlight,
      "last-command-received-on" -> lastCommandReceivedOn,
      "number-of-commands-received" -> numCommandsReceived,
      "number-of-commands-succeeded" -> numCommandsSucceeded,
      "number-of-commands-failed" -> numCommandsFailed).subReport("command-status-events-stats",
        "num-command-status-events-offered" -> numCommandStatusEventsOffered,
        "num-command-status-events-delivered" -> numCommandStatusEventsDelivered,
        "num-command-status-events-requested" -> totalSuppliesRequested,
        "number-of-buffered-command-status-changed-events-to-dispatch" -> bufferedCommandStatusEventsToDispatch.size,
        "throttled" -> throttled,
        "throttled-since" -> throttlingSince,
        "last-command-status-events-delivered-on" -> lastCommandStatusEventsDeliveredOn,
        "last-command-status-events-offered-on" -> lastCommandStatusEventsOfferedOn).subReport("overdue-drones",
          "number-of-overdue-actions" -> overdueActions.size,
          overdueAggIds).subReport("misc",
            "number-of-drones" -> this.context.children.size,
            "amount-of-jettisoned-cargo-since-last-report" -> numJet).configSection(
              "max-parallelism" -> maxParallelism,
              "enqueued-events-throttling-threshold" -> enqueuedEventsThrottlingThreshold,
              "snapshot-storage" -> snapshotting.map(_._1.path.toStringWithoutAddress),
              "aggregate-event-log" -> aggregateEventLog.map(_.path.toStringWithoutAddress))

    scalaz.Success(rep)
  }

  override def preStart() {
    super.preStart()
    logInfo(s""" |Starting...
                 |
                 |max-parallelism: $maxParallelism
                 |enqueued-events-throttling-threshold: $enqueuedEventsThrottlingThreshold""".stripMargin)
    context.parent ! ActorMessages.ConsiderMeForReporting
    self ! Resolve
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    cancelContract()
    context.parent ! ActorMessages.ConsiderMeForReporting
    reportCriticalFailure(reason)
    logWarning(s"[Restart]: Received $numCommandsReceived commands. $numCommandsSucceeded succeeded, $numCommandsFailed failed.")
  }

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    registerComponentControl()
    self ! Resolve
  }

  override def postStop() {
    super.postStop()
    cancelContract()
    deregisterComponentControl()
    logWarning(s"Stopped. Received $numCommandsReceived commands. $numCommandsSucceeded succeeded, $numCommandsFailed failed.")
  }

}