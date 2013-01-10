package almhirt.util.impl

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common.AlmValidation
import almhirt.almvalidation.funs._
import almhirt.environment._
import almhirt.commanding._
import almhirt.environment.configuration._
import almhirt.util._
import almhirt.messaging._
import almhirt.parts.CommandExecutor

class CommandEndpointWithUuidTickets(forwardCommand: CommandEnvelope => Unit, operationStateTracker: ActorRef, theAlmhirt: Almhirt) extends CommandEndpoint {
  private case class RegisterCallback(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit, atMost: FiniteDuration)

  private class Dispatcher extends Actor {
    private val callbacks = new scala.collection.mutable.HashMap[TrackingTicket, AlmValidation[ResultOperationState] => Unit]()

    def receive = {
      case RegisterCallback(ticket, callback, atMost) =>
        callbacks += (ticket -> callback)
        operationStateTracker ! RegisterResultCallbackQry(ticket, self, atMost)
      case OperationStateResultRsp(ticket, state) =>
        callbacks.get(ticket).foreach { callback => callback(state); callbacks -= ticket }
    }
  }

  private val dispatcher = theAlmhirt.system.actorSystem.actorOf(Props(new Dispatcher), "CommandEndpointWithUuidTicketsDispatcher")

  def execute(cmd: DomainCommand) { forwardCommand(CommandEnvelope(cmd, None)) }
  def executeTracked(cmd: DomainCommand) = {
    val ticket = UuidTrackingTicket(theAlmhirt.getUuid)
    forwardCommand(CommandEnvelope(cmd, Some(ticket)))
    ticket
  }
  def executeWithCallback(atMost: FiniteDuration)(cmd: DomainCommand, callback: AlmValidation[ResultOperationState] => Unit) {
    val ticket = executeTracked(cmd)
    dispatcher ! RegisterCallback(ticket, callback, atMost)
  }
}

class CommandEndpointWithUuidTicketsFactory {
  def createCommandEndpoint(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] = {
    for {
      config <- ConfigHelper.commandEndpoint.getConfig(theAlmhirt.system.config)
      modeStr <- ConfigHelper.getString(config)("mode")
      mode <- CommandEndpointForwardMode.fromString(modeStr)
      forwardAction <- mode match {
        case BroadcastCommandOnMessageHub =>
          theAlmhirt.getService[MessageHub].map(hub => (cmdEnv: CommandEnvelope) => hub.broadcast(theAlmhirt.createMessage(cmdEnv)))
        case PostCommandOnMessageHub =>
          theAlmhirt.getService[MessageHub].map(hub => (cmdEnv: CommandEnvelope) => hub.post(theAlmhirt.createMessage(cmdEnv)))
        case PostCommandOnCommandChannel =>
          theAlmhirt.getService[CommandChannel].map(channel => (cmdEnv: CommandEnvelope) => channel.post(theAlmhirt.createMessage(cmdEnv)))
        case PushCommandDirectlyToExecutor =>
          theAlmhirt.getService[CommandExecutor].map(executor => (cmdEnv: CommandEnvelope) => executor.executeCommand(cmdEnv))
      }
      trackerActorName <- ConfigHelper.operationState.getConfig(theAlmhirt.system.config).map(opStateConf => ConfigHelper.operationState.getActorName(opStateConf))
      tracker <- inTryCatch { theAlmhirt.system.actorSystem.actorFor("/user/" + trackerActorName) }
    } yield new CommandEndpointWithUuidTickets(forwardAction, tracker, theAlmhirt)
  }

}