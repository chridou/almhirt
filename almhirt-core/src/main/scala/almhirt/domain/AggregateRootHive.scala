package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.tracking._
import org.reactivestreams.api.Producer
import akka.stream.actor.ActorConsumer
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
  override val buffersize: Int,
  override val droneFactory: AggregateRootDroneFactory,
  override val eventsBroker: StreamBroker[Event])(implicit override val ccuad: CanCreateUuidsAndDateTimes, override val futuresContext: ExecutionContext)
  extends ActorContractor[Event] with ActorLogging with ActorConsumer with AggregateRootHiveSkeleton {

  override val requestStrategy = akka.stream.actor.ActorConsumer.ZeroRequestStrategy

}

private[almhirt] trait AggregateRootHiveSkeleton extends  ActorContractor[Event]{ me: ActorLogging with ActorConsumer ⇒
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
  def buffersize: Int
  def droneFactory: AggregateRootDroneFactory
  implicit def futuresContext: ExecutionContext
  def eventsBroker: StreamBroker[Event]
  implicit def ccuad: CanCreateUuidsAndDateTimes

  private var numReceivedInternal = 0
  private var numSucceededInternal = 0
  private var numFailedInternal = 0

  def numReceived = numReceivedInternal
  def numSucceeded = numSucceededInternal
  def numFailed = numFailedInternal

  def receiveInitialize: Receive = {
    case ReadyForDeliveries ⇒
      request(buffersize)
      context.become(receiveRunning(Vector.empty))
  }

  def receiveRunning(bufferedEvents: Vector[Event]): Receive = {
    case ActorConsumer.OnNext(aggregateCommand: AggregateRootCommand) ⇒
      numReceivedInternal += 1
      context.child(aggregateCommand.aggId.value) match {
        case Some(drone) ⇒
          drone ! aggregateCommand
        case None ⇒
          droneFactory(aggregateCommand) match {
            case scalaz.Success(props) ⇒
              val drone = context.actorOf(props, aggregateCommand.aggId.value)
              //context watch drone
              drone ! aggregateCommand
            case scalaz.Failure(problem) ⇒
              throw new Exception(s"Could not create a drone for command ${aggregateCommand.header}:\n$problem")
          }
      }
      context.become(receiveRunning(bufferedEvents))

    case rsp: AggregateRootDroneInternalMessages.ExecuteCommandResponse ⇒
      if (rsp.isSuccess) {
        numSucceededInternal += 1
      } else {
        numFailedInternal += 1
      }
      offer(1)
      val event: Event = rsp match {
        case AggregateRootDroneInternalMessages.CommandExecuted(command) =>
          CommandSuccessfullyExecuted(command)
        case AggregateRootDroneInternalMessages.CommandNotExecuted(command, problem) =>
          CommandExecutionFailed(command, problem)
        case AggregateRootDroneInternalMessages.Busy(command) =>
          CommandExecutionFailed(command, CollisionProblem("Command can not be executed since another command is being executed."))
      }
      context.become(receiveRunning(bufferedEvents :+ event))

    case OnDeliverSuppliesNow(amount) =>
      val toDeliverNow = bufferedEvents.take(amount)
      val rest = bufferedEvents.drop(toDeliverNow.size)
      deliver(toDeliverNow)
      request(toDeliverNow.size)
      context.become(receiveRunning(rest))

    case ActorConsumer.OnNext(something) ⇒
      log.warning(s"Received something I cannot handle: $something")
      request(1)

    case ActorConsumer.OnComplete ⇒
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

  override def postRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    signContract(eventsBroker)
  }

  override def postStop() {
    super.postStop()
    cancelContract()
    log.info(s"Received $numReceived commands. $numSucceeded succeeded, $numFailed failed.")
  }
  
}