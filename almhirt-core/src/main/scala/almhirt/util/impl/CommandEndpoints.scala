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
  implicit val hasExecutionContext = theAlmhirt

  def execute(cmd: DomainCommand) { forwardCommand(CommandEnvelope(cmd, None)) }
  def executeTracked(cmd: DomainCommand) = {
    val ticket = UuidTrackingTicket(theAlmhirt.getUuid)
    forwardCommand(CommandEnvelope(cmd, Some(ticket)))
    ticket
  }

  def executeWithResult(cmd: CommandWithMaxResponseDuration): AlmFuture[ResultOperationState] = {
    val dur = cmd.maxResponseDuration.getOrElse(theAlmhirt.durations.extraLongDuration)
    val ticket = UuidTrackingTicket(theAlmhirt.getUuid)
    val future = (operationStateTracker ? RegisterResultCallbackQry(ticket))(dur)
    forwardCommand(CommandEnvelope(cmd.command, Some(ticket)))
    future.mapToSuccessfulAlmFuture[OperationStateResultRsp].mapV(x => x.state)
  }
}

class CommandEndpointWithUuidTicketsFactory {
  def createCommandEndpoint(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] = {
    for {
      rootConf <- theAlmhirt.getConfig
      componentConfig <- ConfigHelper.commandEndpoint.getConfig(rootConf)
      operationStateActorName <- ConfigHelper.operationState.getActorName(componentConfig).success
      trackerActor <- inTryCatch { theAlmhirt.actorSystem.actorFor("/user/" + operationStateActorName) }
    } yield {
      theAlmhirt.log.info(s"CommandEndpoint is CommandEndpointWithUuidTickets. Name of used OperationStateTracker is '$operationStateActorName'")
      new CommandEndpointWithUuidTickets((cmdEnv: CommandEnvelope) => theAlmhirt.messageHub.post(theAlmhirt.createMessage(cmdEnv)), trackerActor, theAlmhirt)
    }
  }

}