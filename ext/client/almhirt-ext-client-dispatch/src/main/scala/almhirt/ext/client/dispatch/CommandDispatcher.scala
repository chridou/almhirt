package almhirt.ext.client.dispatch

import almhirt.common.AlmFuture
import almhirt.commanding.DomainCommand
import almhirt.util.{TrackingTicket, ResultOperationState}
import almhirt.client.CommandDispatcher
import almhirt.client.impl.CommandEndpointUris

class DispatchCommandDispatcher(endpoints: CommandEndpointUris) extends CommandDispatcher {
  def dispatch(cmd: DomainCommand){ sys.error("") }
  def dispatchTracked(cmd: DomainCommand): AlmFuture[TrackingTicket] = sys.error("")
  def dispatchAndGetState(cmd: DomainCommand): AlmFuture[ResultOperationState] = sys.error("")
}