package almhirt.util

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.commanding.DomainCommand
import almhirt.environment.Almhirt
import almhirt.common.AlmValidation
import almhirt.common.UnspecifiedProblem

trait CommandEndpointForwardMode
case object BroadcastCommandOnMessageHub extends CommandEndpointForwardMode
case object PostCommandOnMessageHub extends CommandEndpointForwardMode
case object PostCommandOnCommandChannel extends CommandEndpointForwardMode
case object PushCommandDirectlyToExecutor extends CommandEndpointForwardMode

object CommandEndpointForwardMode {
  def fromString(str: String): AlmValidation[CommandEndpointForwardMode] =
    str.toLowerCase() match {
      case "broadcast" => BroadcastCommandOnMessageHub.success
      case "post" => PostCommandOnMessageHub.success
      case "commandchannel" => PostCommandOnCommandChannel.success
      case "executor" => PushCommandDirectlyToExecutor.success
      case x => BadDataProblem("%s does not match any CommandEndpointForwardMode".format(x)).failure
    }
}

trait CommandEndpoint {
  def execute(cmd: DomainCommand): Unit
  def executeTracked(cmd: DomainCommand): TrackingTicket
  def executeWithResult(atMost: FiniteDuration)(cmd: DomainCommand): AlmFuture[ResultOperationState]
}

object CommandEndpoint {
  def apply(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] = {
    new impl.CommandEndpointWithUuidTicketsFactory().createCommandEndpoint(theAlmhirt)
  }
}

trait CommandEndpointFactory {
  def createCommandEndpoint(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint]
}