package almhirt.ext.client.dispatch

import almhirt.common.AlmFuture
import almhirt.environment.Almhirt
import almhirt.commanding.DomainCommand
import almhirt.util.{ TrackingTicket, ResultOperationState }
import almhirt.client.CommandDispatcher
import almhirt.client.impl.CommandEndpointUris
import riftwarp._
import dispatch._
import riftwarp.http.HttpContentType

/**
 * Currently blocking....
 */
//class DispatchCommandDispatcher(endpointUris: CommandEndpointUris, channel: RiftHttpChannel)(implicit theAlmhirt: Almhirt) extends CommandDispatcher {
//  private val nice = true
//  def dispatch(cmd: DomainCommand): AlmFuture[Unit] = {
//    AlmFuture {
//      for{
//        riftWarp <- theAlmhirt.getService[RiftWarp]
//        req <- DispatchFuns.configureRequest(riftWarp)(nice)(channel, cmd, url(endpointUris.executeAndForget.toString()).PUT)
//        resp <- Http(req OK as.String).either.
//      } yield resp
//        sys.error("")
//      }
//    }
//  }
//
//  def dispatchTracked(cmd: DomainCommand): AlmFuture[TrackingTicket] = sys.error("")
//  def dispatchAndGetState(cmd: DomainCommand): AlmFuture[ResultOperationState] = sys.error("")
//}