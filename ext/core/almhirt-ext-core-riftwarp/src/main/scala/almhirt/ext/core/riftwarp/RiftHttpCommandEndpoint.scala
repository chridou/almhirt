package almhirt.ext.core.riftwarp

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.http._
import almhirt.util._
import almhirt.client.CommandDispatcher
import almhirt.commanding.DomainCommand
import riftwarp._
import riftwarp.http._

class RiftHttpCommandCommunicator(endpointAdapter: RiftHttpCommandEndpointAdapter) {
  def dispatch(request: RiftHttpData): AlmFuture[RiftHttpResponse] = endpointAdapter.forward(request)
  def dispatchTracked(request: RiftHttpData): AlmFuture[RiftHttpResponse] = endpointAdapter.forwardTracked(request)
  def dispatchAndGetState(request: RiftHttpData): AlmFuture[RiftHttpResponse] = endpointAdapter.forwardWithResultResponse(request)
}

class RiftHttpCommandEndpointAdapter(endpoint: CommandEndpoint, settings: RiftWarpHttpFuns.RiftHttpFunsSettings, atMost: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext) {
  def forward(req: RiftHttpData): AlmFuture[RiftHttpResponse] = AlmFuture {
    RiftWarpHttpFuns.processRequest[DomainCommand, AnyRef](
      settings,
      () => req.success,
      Http_202_Accepted,
      cmd => { endpoint.execute(cmd); None.success }).success
  }

  def forwardTracked(req: RiftHttpData): AlmFuture[RiftHttpResponse] = AlmFuture {
    RiftWarpHttpFuns.processRequest[DomainCommand, TrackingTicket](
      settings,
      () => req.success,
      Http_200_OK,
      cmd => Some(endpoint.executeTracked(cmd)).success).success
  }

  def forwardWithResultResponse(req: RiftHttpData): AlmFuture[RiftHttpResponse] =
    RiftWarpHttpFuns.processRequestRespondOnFuture[DomainCommand, ResultOperationState](
      settings,
      () => req.success,
      Http_200_OK, cmd =>
        endpoint.executeWithResult(atMost)(cmd).map(Some(_)))
}

class RiftHttpCommandDispatcher(communicator: RiftHttpCommandCommunicator, settings: RiftWarpHttpFuns.RiftHttpFunsSettings, atMost: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext) extends CommandDispatcher {
  override def dispatch(cmd: DomainCommand): AlmFuture[Unit] =
    for {
      request <- AlmFuture { RiftWarpHttpFuns.createHttpData[DomainCommand](settings)(cmd, None) }
      response <- communicator.dispatch(request)
      result <- response match {
        case RiftHttpResponse(Http_202_Accepted, _) => AlmFuture.successful(())
        case RiftHttpResponse(_, data) => AlmFuture.promise { RiftWarpHttpFuns.transformResponse[AnyRef](settings, response) }
      }
    } yield ()

  override def dispatchTracked(cmd: DomainCommand): AlmFuture[TrackingTicket] =
    for {
      request <- AlmFuture { RiftWarpHttpFuns.createHttpData[DomainCommand](settings)(cmd, None) }
      response <- communicator.dispatchTracked(request)
      result <- AlmFuture.promise { RiftWarpHttpFuns.transformResponse[TrackingTicket](settings, response) }
    } yield result

  override def dispatchAndGetState(cmd: DomainCommand): AlmFuture[ResultOperationState] =
    for {
      request <- AlmFuture { RiftWarpHttpFuns.createHttpData[DomainCommand](settings)(cmd, None) }
      response <- communicator.dispatchAndGetState(request)
      result <- AlmFuture.promise { RiftWarpHttpFuns.transformResponse[ResultOperationState](settings, response) }
    } yield result
}