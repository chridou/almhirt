package almhirt.client

import almhirt.common._
import almhirt.commanding.DomainCommand
import almhirt.util._

trait CommandDispatcher {
  def dispatch(cmd: DomainCommand): Unit
  def dispatchTracked(cmd: DomainCommand): AlmFuture[TrackingTicket]
  def dispatchAndGetState(cmd: DomainCommand): AlmFuture[ResultOperationState]
}