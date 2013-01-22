package almhirt.ext.client.dispatch

import almhirt.common._
import almhirt.http._
import almhirt.environment.Almhirt
import almhirt.commanding.DomainCommand
import almhirt.util.{ TrackingTicket, ResultOperationState }
import almhirt.client.CommandDispatcher
import almhirt.client.impl.CommandEndpointUris
import riftwarp._
import riftwarp.http._
import riftwarp.http.RiftWarpHttpFuns.RiftHttpFunsSettings
import dispatch._

/**
 * Currently blocking....
 */
class DispatchCommandDispatcher(endpointUris: CommandEndpointUris, settings: RiftHttpFunsSettings)(implicit theAlmhirt: Almhirt) extends CommandDispatcher {
  def dispatch(cmd: DomainCommand): AlmFuture[Unit] =
    for {
      request <- AlmFuture {
        for {
          riftWarp <- theAlmhirt.getService[RiftWarp]
          req <- DispatchFuns.configureRequest(settings)(cmd, None, url(endpointUris.executeAndForget.toString()).PUT)
        } yield req
      }
      respData <- DispatchFuns.awaitResponseData(settings.contentTypePrefix)(request)
      res <- respData match {
        case RiftHttpResponse(Http_202_Accepted, _) => AlmFuture.successful(())
        case RiftHttpResponse(_, data) => DispatchFuns.transformResponse[AnyRef](settings)(respData)
      }
    } yield ()

  def dispatchTracked(cmd: DomainCommand): AlmFuture[TrackingTicket] =
    for {
      request <- AlmFuture {
        for {
          riftWarp <- theAlmhirt.getService[RiftWarp]
          req <- DispatchFuns.configureRequest(settings)(cmd, None, url(endpointUris.executeTracked.toString()).PUT)
        } yield req
      }
      result <- DispatchFuns.getResponseResult[TrackingTicket](settings, request)
    } yield result

  def dispatchAndGetState(cmd: DomainCommand): AlmFuture[ResultOperationState] =
    for {
      request <- AlmFuture {
        for {
          riftWarp <- theAlmhirt.getService[RiftWarp]
          req <- DispatchFuns.configureRequest(settings)(cmd, None, url(endpointUris.executeAndResult.toString()).PUT)
        } yield req
      }
      result <- DispatchFuns.getResponseResult[ResultOperationState](settings, request)
    } yield result
}