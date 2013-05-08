package almhirt.util

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.commanding.DomainCommand
import almhirt.core.Almhirt
import almhirt.common.AlmValidation
import almhirt.common.UnspecifiedProblem

trait CommandEndpoint {
  def execute(cmd: Command): Unit
  def executeTracked(cmd: Command): TrackingTicket
  def executeWithResult(cmd: CommandWithMaxResponseDuration): AlmFuture[ResultOperationState]
}

object CommandEndpoint {
  def apply(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] = {
    new impl.CommandEndpointWithUuidTicketsFactory().createCommandEndpoint(theAlmhirt)
  }
}

trait CommandEndpointFactory {
  def createCommandEndpoint(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint]
}