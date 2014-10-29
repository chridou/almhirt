package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.tracking._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import org.reactivestreams.{ Publisher }
import akka.stream.actor.{ ActorSubscriber, ActorSubscriberMessage, ZeroRequestStrategy }
import almhirt.streaming._

final case class HiveDescriptor(val value: String) extends AnyVal

object AggregateRootHive {
  def propsRaw(
    hiveDescriptor: HiveDescriptor,
    aggregateEventLogToResolve: ToResolve,
    snapShotStorageToResolve: Option[ToResolve],
    resolveSettings: ResolveSettings,
    commandBuffersize: Int,
    droneFactory: AggregateRootDroneFactory,
    otherThanContextEventBroker: Option[StreamBroker[Event]],
    enqueudEventsThrottlingThresholdFactor: Int = 2)(implicit ctx: AlmhirtContext): Props =
    Props(new AggregateRootHive(
      hiveDescriptor,
      aggregateEventLogToResolve,
      snapShotStorageToResolve,
      resolveSettings,
      commandBuffersize,
      droneFactory,
      otherThanContextEventBroker.getOrElse(ctx.eventBroker),
      enqueudEventsThrottlingThresholdFactor))

  def props(
    hiveDescriptor: HiveDescriptor,
    droneFactory: AggregateRootDroneFactory,
    hiveConfigName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.aggregates.aggregate-root-hive" + hiveConfigName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      aggregateEventLogPath ← section.v[String]("aggregate-event-log-path")
      aggregateEventLogToResolve ← inTryCatch { ResolvePath(ActorPath.fromString(aggregateEventLogPath)) }
      snapShotStoragePath ← section.magicOption[String]("snapshot-storage-path")
      snapShotStorageToResolve ← inTryCatch { snapShotStoragePath.map(path ⇒ ResolvePath(ActorPath.fromString(path))) }
      resolveSettings ← section.v[ResolveSettings]("resolve-settings")
      commandBuffersize ← section.v[Int]("command-buffer-size")
      enqueudEventsThrottlingThresholdFactor ← section.v[Int]("enqueud-events-throttling-threshold-factor")
    } yield propsRaw(
      hiveDescriptor,
      aggregateEventLogToResolve,
      snapShotStorageToResolve,
      resolveSettings,
      commandBuffersize,
      droneFactory,
      Some(ctx.eventBroker),
      enqueudEventsThrottlingThresholdFactor)
  }
}

private[almhirt] object AggregateRootHiveInternals {
}

private[almhirt] class AggregateRootHive(
  override val hiveDescriptor: HiveDescriptor,
  override val aggregateEventLogToResolve: ToResolve,
  override val snapShotStorageToResolve: Option[ToResolve],
  override val resolveSettings: ResolveSettings,
  override val commandBuffersize: Int,
  override val droneFactory: AggregateRootDroneFactory,
  override val eventsBroker: StreamBroker[Event],
  enqueudEventsThrottlingThresholdFactor: Int = 2)(implicit override val almhirtContext: AlmhirtContext)
  extends AlmActor with AlmActorLogging with ActorContractor[Event] with ActorLogging with ActorSubscriber with AggregateRootHiveSkeleton {

  override val requestStrategy = ZeroRequestStrategy
  override val enqueudEventsThrottlingThreshold = commandBuffersize * enqueudEventsThrottlingThresholdFactor

}

private[almhirt] trait AggregateRootHiveSkeleton extends ActorContractor[Event] { me: AlmActor with AlmActorLogging with ActorLogging with ActorSubscriber ⇒
  import AggregateRootHive._
  import AggregateRootHiveInternals._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: AggregateRootEventStoreFailedReadingException ⇒ Restart
      case _: RebuildAggregateRootFailedException ⇒ Restart
      case _: CouldNotDispatchAllAggregateRootEventsException ⇒ Restart
      case _: Exception ⇒ Escalate
    }

  def aggregateEventLogToResolve: ToResolve
  def snapShotStorageToResolve: Option[ToResolve]
  def resolveSettings: ResolveSettings

  def hiveDescriptor: HiveDescriptor
  def commandBuffersize: Int
  def droneFactory: AggregateRootDroneFactory
  implicit val futuresContext: ExecutionContext = almhirtContext.futuresContext
  def eventsBroker: StreamBroker[Event]
  def enqueudEventsThrottlingThreshold: Int

  private var numReceivedInternal = 0
  private var numSucceededInternal = 0
  private var numFailedInternal = 0

  def numReceived = numReceivedInternal
  def numSucceeded = numSucceededInternal
  def numFailed = numFailedInternal

  private case object Resolve
  def receiveResolve: Receive = {
    case Resolve ⇒
      val actorsToResolve =
        Map("aggregateeventlog" -> aggregateEventLogToResolve) ++
          snapShotStorageToResolve.map(r ⇒ Map("snapshotstorage" -> r)).getOrElse(Map.empty)
      context.resolveMany(actorsToResolve, resolveSettings, None, Some("resolver"))

    case ActorMessages.ManyResolved(dependencies, _) ⇒
      logInfo("Found dependencies.")
      signContract(eventsBroker)

      context.become(receiveInitialize(dependencies("aggregateeventlog"), dependencies.get("snapshotstorage")))

    case ActorMessages.ManyNotResolved(problem, _) ⇒
      logError(s"Failed to resolve dependencies:\n$problem")
      reportCriticalFailure(problem)
      sys.error(s"Failed to resolve dependencies.")
  }

  def receiveInitialize(aggregateEventLog: ActorRef, snapshotStorage: Option[ActorRef]): Receive = {
    case ReadyForDeliveries ⇒
      requestCommands()
      context.become(receiveRunning(aggregateEventLog, snapshotStorage))
  }

  private var remainingRequestCapacity: Int = commandBuffersize
  private var bufferedEvents: Vector[Event] = Vector.empty

  private def requestCommands() {
    if (bufferedEvents.size > enqueudEventsThrottlingThreshold) {
      logWarning(s"to many events: ${bufferedEvents.size}")
    }
    if (remainingRequestCapacity > 0 && bufferedEvents.size <= enqueudEventsThrottlingThreshold) {
      request(remainingRequestCapacity)
      remainingRequestCapacity = 0
    }
  }

  private def receivedCommandResponse() {
    remainingRequestCapacity = remainingRequestCapacity + 1
  }

  private def receivedInvalidCommand() {
    remainingRequestCapacity = remainingRequestCapacity + 1
  }

  private def enqueueEvent(event: Event) {
    bufferedEvents = bufferedEvents :+ event
    offer(1)
  }

  private def deliverEvents(amount: Int) {
    val toDeliver = bufferedEvents.take(amount)
    val rest = bufferedEvents.drop(toDeliver.size)
    deliver(toDeliver)
    bufferedEvents = rest
  }

  def receiveRunning(aggregateEventLog: ActorRef, snapshotStorage: Option[ActorRef]): Receive = {
    case ActorSubscriberMessage.OnNext(aggregateCommand: AggregateRootCommand) ⇒
      numReceivedInternal += 1
      val drone = context.child(aggregateCommand.aggId.value) match {
        case Some(drone) ⇒
          drone
        case None ⇒
          droneFactory(aggregateCommand, aggregateEventLog, snapshotStorage) match {
            case scalaz.Success(props) ⇒
              context.actorOf(props, aggregateCommand.aggId.value)
            //context watch drone
            case scalaz.Failure(problem) ⇒ {
              reportCriticalFailure(problem)
              throw new Exception(s"Could not create a drone for command ${aggregateCommand.header}:\n$problem")
            }
          }
      }
      drone ! aggregateCommand
      enqueueEvent(CommandExecutionInitiated(aggregateCommand))

    case rsp: AggregateRootDroneInternalMessages.ExecuteCommandResponse ⇒
      receivedCommandResponse()
      if (rsp.isSuccess) {
        numSucceededInternal += 1
      } else {
        numFailedInternal += 1
      }
      val event: Event = rsp match {
        case AggregateRootDroneInternalMessages.CommandExecuted(command) ⇒
          CommandSuccessfullyExecuted(command)
        case AggregateRootDroneInternalMessages.CommandNotExecuted(command, problem) ⇒
          reportRejectedCommand(command, MinorSeverity, problem)
          CommandExecutionFailed(command, problem)
        case AggregateRootDroneInternalMessages.Busy(command) ⇒
          reportRejectedCommand(command, MinorSeverity, CollisionProblem("Command can not be executed since another command is being executed."))
          CommandExecutionFailed(command, CollisionProblem("Command can not be executed since another command is being executed."))
      }
      enqueueEvent(event)
      requestCommands()

    case OnDeliverSuppliesNow(amount) ⇒
      deliverEvents(amount)
      requestCommands()

    case ActorSubscriberMessage.OnNext(something) ⇒
      reportMinorFailure(ArgumentProblem(s"Received something I cannot handle: $something"))
      log.warning(s"Received something I cannot handle: $something")
      receivedInvalidCommand()

    case ActorSubscriberMessage.OnComplete ⇒
      if (log.isDebugEnabled)
        logDebug(s"Aggregate command stream completed after receiving $numReceived commands. $numSucceeded succeeded, $numFailed failed.")

    case OnBrokerProblem(problem) ⇒
      reportCriticalFailure(problem)
      throw new Exception(s"The broker reported a problem:\n$problem")

    case OnContractExpired ⇒
      logInfo(s"Contract with broker expired. There are ${bufferedEvents.size} events still to deliver.")
  }

  override def receive: Receive = receiveResolve

  override def preStart() {
    super.preStart()
    self ! Resolve
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    cancelContract()
    reportCriticalFailure(reason)
    logInfo(s"[Restart]: Received $numReceived commands. $numSucceeded succeeded, $numFailed failed.")
  }

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    self ! Resolve
  }

  override def postStop() {
    super.postStop()
    cancelContract()
    logInfo(s"Received $numReceived commands. $numSucceeded succeeded, $numFailed failed.")
  }

}