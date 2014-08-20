package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.tracking._
import org.reactivestreams.api.Producer
import akka.stream.actor.ActorConsumer

object AggregateRootHive {
  sealed trait CommandTimeoutSettings
  case object NoCommandTimeouts extends CommandTimeoutSettings
  case class CommandTimeouts(commandTimeout: FiniteDuration, checkForTimeoutsInterval: FiniteDuration) extends CommandTimeoutSettings
}

private[almhirt] object AggregateRootHiveInternals {
  case object Start
}

class AggregateRootHive(
  override val buffersize: Int,
  override val initialCommandTimeout: AggregateRootHive.CommandTimeoutSettings,
  override val droneFactory: AggregateRootDroneFactory,
  override val commandStatusSink: FireAndForgetSink[CommandStatusChanged])(implicit override val ccuad: CanCreateUuidsAndDateTimes, override val futuresContext: ExecutionContext) extends Actor with ActorLogging with ActorConsumer with AggregateRootHiveInternal {

  override val requestStrategy = akka.stream.actor.ActorConsumer.ZeroRequestStrategy

  override def preStart() {
    super.preStart()
    self ! AggregateRootHiveInternals.Start
  }
  
  override def postStop() {
    super.postStop()
    log.info(s"Received $numReceived commands. $numSucceeded succeeded, $numFailed failed, $numTimedOut timed out.")
  }
}

private[almhirt] trait AggregateRootHiveInternal { me: Actor with ActorLogging with ActorConsumer =>
  import AggregateRootHive._
  import AggregateRootHiveInternals._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: AggregateEventStoreFailedReadingException => Restart
      case _: RebuildAggregateRootFailedException => Restart
      case _: CouldNotDispatchAllAggregateEventsException => Restart
      case _: Exception => Escalate
    }

  def initialCommandTimeout: CommandTimeoutSettings
  def buffersize: Int
  def droneFactory: AggregateRootDroneFactory
  implicit def futuresContext: ExecutionContext
  def commandStatusSink: FireAndForgetSink[CommandStatusChanged]
  implicit def ccuad: CanCreateUuidsAndDateTimes

  private case class CheckForTimeouts(commandTimeout: FiniteDuration)
  private case class TimedOutCommands(timedOut: Map[CommandId, (FiniteDuration, CommandHeader)])

  private var numReceivedInternal = 0
  private var numSucceededInternal = 0
  private var numFailedInternal = 0
  private var numTimedOutInternal = 0

  def numReceived = numReceivedInternal
  def numSucceeded = numSucceededInternal
  def numFailed = numFailedInternal
  def numTimedOut = numTimedOutInternal

  private def initiateCommandTimeoutChecking(settings: CommandTimeoutSettings) {
    settings match {
      case NoCommandTimeouts => ()
      case CommandTimeouts(commandTimeout, checkForTimeoutsInterval) =>
        context.system.scheduler.scheduleOnce(checkForTimeoutsInterval, self, CheckForTimeouts(commandTimeout))

    }
  }

  private[this] var commandTimeout: CommandTimeoutSettings = NoCommandTimeouts
  def receiveInitialize: Receive = {
    case Start =>
      commandTimeout = initialCommandTimeout
      request(buffersize)
      initiateCommandTimeoutChecking(commandTimeout)
      context.become(receiveRunning(Map.empty))
  }

  def receiveRunning(executing: Map[CommandId, (Deadline, CommandHeader)]): Receive = {
    case ActorConsumer.OnNext(aggregateCommand: AggregateCommand) =>
      numReceivedInternal += 1
      context.child(aggregateCommand.aggId.value) match {
        case Some(drone) =>
          drone ! aggregateCommand
        case None =>
          droneFactory(aggregateCommand) match {
            case scalaz.Success(props) =>
              val drone = context.actorOf(props, aggregateCommand.aggId.value)
              //context watch drone
              drone ! aggregateCommand
            case scalaz.Failure(problem) =>
              throw new Exception(s"Could not create a drone for command ${aggregateCommand.header}:\n$problem")
          }
      }
      context.become(receiveRunning(executing + (aggregateCommand.commandId -> (Deadline.now, aggregateCommand.header))))

    case rsp: AggregateRootDroneInternalMessages.ExecuteCommandResponse =>
      if (executing.contains(rsp.commandHeader.id)) {
        if (rsp.isSuccess) {
          numSucceededInternal += 1
        } else {
          numFailedInternal += 1
        }
        request(1)
        context.become(receiveRunning(executing - rsp.commandHeader.id))
      } else {
        log.warning(s"Received a command response for command ${rsp.commandHeader} which I don't know. It might have timed out some time ago.")
      }

    case ActorConsumer.OnNext(something) =>
      log.warning(s"Received something I cannot handle: $something")
      request(1)

    case ActorConsumer.OnComplete =>
      if (log.isDebugEnabled)
        log.debug(s"Aggregate command stream completed after receiving $numReceived commands. $numSucceeded succeeded, $numFailed failed, $numTimedOut timed out.")

    case CheckForTimeouts(commandTimeout) =>
      AlmFuture.compute {
        val deadline = Deadline.now - commandTimeout
        val now = Deadline.now
        val timedOut = executing
          .filter(_._2._1 < deadline)
          .map { case (id, (born, header)) => (id, (now - born, header)) }
        self ! TimedOutCommands(timedOut)
      }

    case TimedOutCommands(timedOut) =>
      val reallyTimedOutNow = executing.keySet.foldLeft(timedOut) { case (acc, cur) => acc - cur }
      numTimedOutInternal += reallyTimedOutNow.size
      if (!reallyTimedOutNow.isEmpty) {
        reallyTimedOutNow.foreach { timedOut =>
          commandStatusSink(CommandFailed(
            EventHeader(),
            timedOut._2._2,
            OperationTimedOutProblem(s"The command timed out by ${timedOut._2._1.defaultUnitString}.")))

          if (log.isDebugEnabled)
            log.debug(s"Command ${timedOut._1} timed out after ${timedOut._2._1.defaultUnitString}.")
        }
        log.warning(s"${reallyTimedOutNow.size} commands timed out.")
        val newExecuting = reallyTimedOutNow.foldLeft(executing) { case (acc, cur) => acc - cur._1 }
        request(reallyTimedOutNow.size)
        context.become(receiveRunning(newExecuting))
      }
      initiateCommandTimeoutChecking(commandTimeout)
  }

  override def receive: Receive = receiveInitialize
}