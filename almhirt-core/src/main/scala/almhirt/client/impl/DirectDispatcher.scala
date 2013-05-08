package almhirt.client.impl

import almhirt.common._
import almhirt.core.Almhirt
import almhirt.util._
import almhirt.client._
import almhirt.commanding.DomainCommand

class DirectDispatcher(endpoint: CommandEndpoint) extends CommandDispatcher {
  override def dispatch(cmd: DomainCommand): AlmFuture[Unit] = AlmFuture.successful(endpoint.execute(cmd))
  override def dispatchTracked(cmd: DomainCommand) = AlmFuture.successful(endpoint.executeTracked(cmd))
  override def dispatchAndGetResult(cmd: CommandWithMaxResponseDuration) = endpoint.executeWithResult(cmd)
}

class DirectDispatcherFactory extends CommandDispatcherFactory {
  def createCommandDispatcher(theAlmhirt: Almhirt): AlmValidation[CommandDispatcher] = {
    theAlmhirt.log.info("CommandDispatcher is DirectDispatcher")
    theAlmhirt.getService[CommandEndpoint].map(endpoint => new DirectDispatcher(endpoint))
  }
}