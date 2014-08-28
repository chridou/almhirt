package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.tracking._
import org.reactivestreams.{ Publisher }
import akka.stream.actor.{ ActorSubscriber, ActorSubscriberMessage, ZeroRequestStrategy }
import almhirt.streaming._

class HiveDescriptor(val value: String) extends AnyVal

object HiveDescriptor {
  def apply(value: String) = new HiveDescriptor(value)
}

object AggregateRootHive {
}

private[almhirt] object AggregateRootHiveInternals {
}

class AggregateRootHive(
  override val hiveDescriptor: HiveDescriptor,
  override val commandBuffersize: Int,
  override val droneFactory: AggregateRootDroneFactory,
  override val eventsBroker: StreamBroker[Event],
  enqueudEventsThrottlingThresholdFactor: Int = 2)(implicit override val ccuad: CanCreateUuidsAndDateTimes, override val futuresContext: ExecutionContext)
  extends ActorContractor[Event] with ActorLogging with ActorSubscriber with AggregateRootHiveSkeleton {

  override val requestStrategy = ZeroRequestStrategy
  override val enqueudEventsThrottlingThreshold = commandBuffersize * enqueudEventsThrottlingThresholdFactor

}

private[almhirt] trait AggregateRootHiveSkeleton extends ActorContractor[Event] { me: ActorLogging with ActorSubscriber ⇒
  import AggregateRootHive._
  import AggregateRootHiveInternals._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: AggregateEventStoreFailedReadingException ⇒ Restart
      case _: RebuildAggregateRootFailedException ⇒ Restart
      case _: CouldNotDispatchAllAggregateEventsException ⇒ Restart
      case _: Exception ⇒ Escalate
    }

  def hiveDescriptor: HiveDescriptor
  def commandBuffersize: Int
  def droneFactory: AggregateRootDroneFactory
  implicit def futuresContext: ExecutionContext
  def eventsBroker: StreamBroker[Event]
  def enqueudEventsThrottlingThreshold: Int
  implicit def ccuad: CanCreateUuidsAndDateTimes

  private var numReceivedInternal = 0
  private var numSucceededInternal = 0
  private var numFailedInternal = 0

  def numReceived = numReceivedInternal
  def numSucceeded = numSucceededInternal
  def numFailed = numFailedInternal

  def receiveInitialize: Receive = {
    case ReadyForDeliveries ⇒
      requestCommands()
      context.become(receiveRunning())
  }


  private var remainingRequestCapacity: Int = commandBuffersize
  private var bufferedEvents: Vector[Event] = Vector.empty

  private def requestCommands() {
    if(bufferedEvents.size > enqueudEventsThrottlingThreshold) {
      log.warning(s"to many events: ${bufferedEvents .size}")
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

  def receiveRunning(): Receive = {
    case ActorSubscriberMessage.OnNext(aggregateCommand: AggregateRootCommand) ⇒
      numReceivedInternal += 1
      val drone = context.child(aggregateCommand.aggId.value) match {
        case Some(drone) ⇒
          drone
        case None ⇒
          droneFactory(aggregateCommand) match {
            case scalaz.Success(props) ⇒
              context.actorOf(props, aggregateCommand.aggId.value)
            //context watch drone
            case scalaz.Failure(problem) ⇒
              throw new Exception(s"Could not create a drone for command ${aggregateCommand.header}:\n$problem")
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
        case AggregateRootDroneInternalMessages.CommandExecuted(command) =>
          CommandSuccessfullyExecuted(command)
        case AggregateRootDroneInternalMessages.CommandNotExecuted(command, problem) =>
          CommandExecutionFailed(command, problem)
        case AggregateRootDroneInternalMessages.Busy(command) =>
          CommandExecutionFailed(command, CollisionProblem("Command can not be executed since another command is being executed."))
      }
      enqueueEvent(event)
      requestCommands()

    case OnDeliverSuppliesNow(amount) =>
      deliverEvents(amount)
      requestCommands()

    case ActorSubscriberMessage.OnNext(something) ⇒
      log.warning(s"Received something I cannot handle: $something")
      receivedInvalidCommand()

    case ActorSubscriberMessage.OnComplete ⇒
      if (log.isDebugEnabled)
        log.debug(s"Aggregate command stream completed after receiving $numReceived commands. $numSucceeded succeeded, $numFailed failed.")

    case OnBrokerProblem(problem) =>
      throw new Exception(s"The broker reported a problem:\n$problem")

    case OnContractExpired =>
      log.info(s"Contract with broker expired. There are ${bufferedEvents.size} events still to deliver.")
  }

  override def receive: Receive = receiveInitialize

  override def preStart() {
    super.preStart()
    signContract(eventsBroker)
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    cancelContract()
    log.info(s"[Restart]: Received $numReceived commands. $numSucceeded succeeded, $numFailed failed.")
  }

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    signContract(eventsBroker)
  }

  override def postStop() {
    super.postStop()
    cancelContract()
    log.info(s"Received $numReceived commands. $numSucceeded succeeded, $numFailed failed.")
  }

}