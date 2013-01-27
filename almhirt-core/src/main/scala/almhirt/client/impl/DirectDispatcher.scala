package almhirt.client.impl

import almhirt.common._
import almhirt.core.Almhirt
import almhirt.util._
import almhirt.client._
import almhirt.commanding.DomainCommand

class DirectDispatcher(endpoint: CommandEndpoint, atMost: scala.concurrent.duration.FiniteDuration) extends CommandDispatcher {
  override def dispatch(cmd: DomainCommand): AlmFuture[Unit] = AlmFuture.successful(endpoint.execute(cmd))
  override def dispatchTracked(cmd: DomainCommand) = AlmFuture.successful(endpoint.executeTracked(cmd))
  override def dispatchAndGetState(cmd: DomainCommand) = endpoint.executeWithResult(atMost)(cmd)
}

class DirectDispatcherFactory extends CommandDispatcherFactory {
  def createCommandDispatcher(theAlmhirt: Almhirt): AlmValidation[CommandDispatcher] =
    theAlmhirt.getService[CommandEndpoint].map(endpoint => new DirectDispatcher(endpoint, theAlmhirt.defaultDuration))
}