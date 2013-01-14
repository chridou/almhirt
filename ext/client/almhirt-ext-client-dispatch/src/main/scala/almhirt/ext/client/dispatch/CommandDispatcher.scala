package almhirt.ext.client.dispatch

import almhirt.common.AlmFuture
import almhirt.commanding.DomainCommand
import almhirt.util.{TrackingTicket, ResultOperationState}
import almhirt.client.CommandDispatcher
import almhirt.client.impl.CommandEndpointUris
import dispatch._

class DispatchCommandDispatcher(endpointUris: CommandEndpointUris) extends CommandDispatcher {
  def dispatch(cmd: DomainCommand): AlmFuture[Unit] = { 
//     val svc = url(endpointUris.executeAndForget.toString()).POST.addHeader(name, value)
//     val country = Http(svc OK as.String)
     sys.error("")
  }
  
  def dispatchTracked(cmd: DomainCommand): AlmFuture[TrackingTicket] = sys.error("")
  def dispatchAndGetState(cmd: DomainCommand): AlmFuture[ResultOperationState] = sys.error("")
}