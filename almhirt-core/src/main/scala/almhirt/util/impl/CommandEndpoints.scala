package almhirt.util.impl

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.funs._
import almhirt.environment._
import almhirt.commanding._
import almhirt.environment.configuration._
import almhirt.util._
import almhirt.messaging._
import almhirt.parts.CommandExecutor
import almhirt.core.Almhirt

class CommandEndpointWithUuidTickets(forwardCommand: CommandEnvelope => Unit, operationStateTracker: ActorRef, theAlmhirt: Almhirt) extends CommandEndpoint {
  private case class RegisterForTicket(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit, atMost: FiniteDuration)
  implicit val hasExecutionContext = theAlmhirt

  def execute(cmd: DomainCommand) { forwardCommand(CommandEnvelope(cmd, None)) }
  def executeTracked(cmd: DomainCommand) = {
    val ticket = UuidTrackingTicket(theAlmhirt.getUuid)
    forwardCommand(CommandEnvelope(cmd, Some(ticket)))
    ticket
  }

  def executeWithResult(atMost: FiniteDuration)(cmd: DomainCommand): AlmFuture[ResultOperationState] = {
    val ticket = UuidTrackingTicket(theAlmhirt.getUuid)
    val future = (operationStateTracker ? RegisterResultCallbackQry(ticket, atMost))(atMost)
    forwardCommand(CommandEnvelope(cmd, Some(ticket)))
    future.mapToSuccessfulAlmFuture[OperationStateResultRsp].mapV(x => x.state)
  }
}

class CommandEndpointWithUuidTicketsFactory {
  def createCommandEndpoint(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] = {
    for {
      rootConf <- theAlmhirt.getConfig
      componentConfig <- ConfigHelper.commandEndpoint.getConfig(rootConf)
      modeStr <- ConfigHelper.getString(componentConfig)("mode")
      mode <- CommandEndpointForwardMode.fromString(modeStr)
      forwardAction <- mode match {
        case BroadcastCommandOnMessageHub =>
          ((cmdEnv: CommandEnvelope) => theAlmhirt.messageHub.broadcast(theAlmhirt.createMessage(cmdEnv))).success
        case PostCommandOnMessageHub =>
          ((cmdEnv: CommandEnvelope) => theAlmhirt.messageHub.post(theAlmhirt.createMessage(cmdEnv))).success
        case PostCommandOnCommandChannel =>
          theAlmhirt.getService[CommandChannel].map(channel => (cmdEnv: CommandEnvelope) => channel.post(theAlmhirt.createMessage(cmdEnv)))
        case PushCommandDirectlyToExecutor =>
          theAlmhirt.getService[CommandExecutor].map(executor => (cmdEnv: CommandEnvelope) => executor.executeCommand(cmdEnv))
      }
      endpointActorName <- ConfigHelper.commandEndpoint.getActorName(componentConfig).success
      endpoint <- inTryCatch { theAlmhirt.actorSystem.actorFor("/user/" + endpointActorName) }
    } yield {
      theAlmhirt.log.info(s"CommandEndpoint is CommandEndpointWithUuidTickets. Name is '$endpointActorName', mode is '$mode'")
      new CommandEndpointWithUuidTickets(forwardAction, endpoint, theAlmhirt)
    }
  }

}