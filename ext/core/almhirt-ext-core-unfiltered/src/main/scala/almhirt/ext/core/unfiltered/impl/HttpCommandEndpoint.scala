package almhirt.ext.core.unfiltered.impl

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.environment.Almhirt
import almhirt.commanding.DomainCommand
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered._
import almhirt.http.impl.JustForTestingProblemLaundry
import almhirt.http._
import riftwarp._
import riftwarp.http._
import unfiltered.request._
import unfiltered.response._
import almhirt.util.TrackingTicket
import almhirt.util.ResultOperationState

class HttpCommandEndpoint(getEndpoint: () => AlmValidation[CommandEndpoint], settings: RiftWarpHttpFuns.RiftHttpFunsSettings, theAlmhirt: Almhirt) extends ForwardsCommandsFromHttpRequest {
  implicit val hasExecutionContext = theAlmhirt

  protected def processRequest[TResp <: AnyRef](okStatus: HttpSuccess, computeResponse: (DomainCommand, CommandEndpoint) => AlmValidation[Option[TResp]])(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) =
    UnfilteredFuns.processRequest[DomainCommand, TResp](settings, okStatus, cmd => getEndpoint().flatMap(endPoint => computeResponse(cmd, endPoint)), req, responder)

  protected def forwardHandler = processRequest[AnyRef](Http_202_Accepted, (cmd, endpoint) =>
    { endpoint.execute(cmd); scalaz.Success(None) })_

  protected def forwardTrackedHandler = processRequest[TrackingTicket](Http_202_Accepted, (cmd, endpoint) =>
    Some(endpoint.executeTracked(cmd)).success)_

  protected def forwardWithResultResponseHandler(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) =
    UnfilteredFuns.processRequestRespondOnFuture[DomainCommand, ResultOperationState](
      settings,
      Http_200_OK,
      cmd => AlmFuture.promise(getEndpoint()).flatMap(endPoint => endPoint.executeWithResult(theAlmhirt.mediumDuration)(cmd).map(Some(_))),
      req, responder)

  def forward(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardHandler(req, responder) }

  def forwardTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardTrackedHandler(req, responder) }

  def forwardWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardWithResultResponseHandler(req, responder) }
}