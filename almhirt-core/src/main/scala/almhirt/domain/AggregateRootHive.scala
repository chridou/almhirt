package almhirt.domain

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.tracking._

import org.reactivestreams.api.Producer
import akka.stream.actor.ActorConsumer

trait AggregateRootDroneFactory extends Function1[AggregateCommand, AlmValidation[Props]] {
  final def apply(command: AggregateCommand): AlmValidation[Props] = propsForCommand(command)
  def propsForCommand(command: AggregateCommand): AlmValidation[Props]
}

private[almhirt] object AggregateRootHiveInternals {
  case object Start
}

trait AggregateRootHive { me: Actor with ActorLogging with ActorConsumer =>
  import AggregateRootHiveInternals._
  def aggregateCommandProducer: Producer[AggregateCommand]

  def commandTimeout: FiniteDuration
  def checkForTimeoutsInterval: FiniteDuration
  def buffersize: Int
  def droneFactory: AggregateRootDroneFactory
  implicit def futuresContext: ExecutionContext
  def commandStatusSink: FireAndForgetSink[CommandStatusChanged]
  implicit def ccuad: CanCreateUuidsAndDateTimes

  private case object CheckForTimeouts
  private case class TimedOutCommands(timedOut: Map[CommandId, (FiniteDuration, CommandHeader)])

  var numReceived = 0
  var numSucceeded = 0
  var numFailed = 0
  var numTimedOut = 0

  def receiveInitialize: Receive = {
    case Start =>
      val meAsConsumer = ActorConsumer[AggregateCommand](self)
      aggregateCommandProducer.produceTo(meAsConsumer)
      request(buffersize)
      context.become(receiveRunning(Map.empty))
      context.system.scheduler.scheduleOnce(checkForTimeoutsInterval, self, CheckForTimeouts)
  }

  def receiveRunning(executing: Map[CommandId, (Deadline, CommandHeader)]): Receive = {
    case ActorConsumer.OnNext(aggregateCommand: AggregateCommand) =>
      numReceived += 1
      context.child(aggregateCommand.aggId.value) match {
        case Some(drone) =>
          drone ! aggregateCommand
        case None =>
          droneFactory(aggregateCommand) match {
            case scalaz.Success(props) =>
              val drone = context.actorOf(props, aggregateCommand.aggId.value)
              context watch drone
              drone ! aggregateCommand
            case scalaz.Failure(problem) =>
              throw new Exception(s"Could not create a drone for command ${aggregateCommand.header}:\n$problem")
          }
      }
      context.become(receiveRunning(executing + (aggregateCommand.commandId -> (commandTimeout.fromNow, aggregateCommand.header))))

    case rsp: AggregateRootDroneInternalMessages.ExecuteCommandResponse =>
      if (executing.contains(rsp.commandHeader.id)) {
        if (rsp.isSuccess) {
          numSucceeded += 1
        } else {
          numFailed += 1
        }
        request(1)
        context.become(receiveRunning(executing - rsp.commandHeader.id))
      } else {
        log.warning(s"Received a command response for command ${rsp.commandHeader} which I don't know. It might have timed out some time ago.")
      }

    case ActorConsumer.OnNext(something) =>
      log.warning(s"Received something I cannot handle: $something")
      request(1)

    case CheckForTimeouts =>
      AlmFuture.compute {
        val now = Deadline.now
        val timedOut = executing
          .filter(_._2._1 < now)
          .map { case (id, (deadline, header)) => (id, (deadline - now, header)) }
        self ! TimedOutCommands(timedOut)
      }

    case TimedOutCommands(timedOut) =>
      val reallyTimedOutNow = executing.keySet.foldLeft(timedOut){ case (acc, cur) => acc - cur }
      numTimedOut += reallyTimedOutNow.size
      if (!reallyTimedOutNow.isEmpty) {
        reallyTimedOutNow.foreach(timedOut => commandStatusSink(CommandFailed(
          EventHeader(),
          timedOut._2._2,
          OperationTimedOutProblem(s"The command timed out by ${timedOut._2._1.defaultUnitString}."))))
        log.warning(s"${reallyTimedOutNow.size} commands timed out.")
        val newExecuting = reallyTimedOutNow.foldLeft(executing) { case (acc, cur) => acc - cur._1 }
        request(reallyTimedOutNow.size)
        context.become(receiveRunning(newExecuting))
      }
      context.system.scheduler.scheduleOnce(checkForTimeoutsInterval, self, CheckForTimeouts)
  }
}