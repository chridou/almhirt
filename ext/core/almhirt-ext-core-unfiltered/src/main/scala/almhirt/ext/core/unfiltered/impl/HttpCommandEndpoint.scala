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

class HttpCommandEndpoint(getEndpoint: () => AlmValidation[CommandEndpoint], riftWarp: RiftWarp, theAlmhirt: Almhirt) extends ForwardsCommandsFromHttpRequest {
  implicit def hasExecutionContext = theAlmhirt
  val settings = RiftWarpHttpFuns.RiftHttpFunsSettings(riftWarp, true, JustForTestingProblemLaundry, theAlmhirt.reportProblem, RiftChannel.Json)

  protected def processRequest[TResp <: AnyRef](okStatus: HttpSuccess, computeResponse: (DomainCommand, CommandEndpoint) => AlmValidation[Option[TResp]])(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) =
    UnfilteredFuns.processRequest[DomainCommand, TResp](settings, okStatus, cmd => getEndpoint().flatMap(endPoint => computeResponse(cmd, endPoint)), req, responder)

  protected def forwardHandler = processRequest[AnyRef](Http_202_Accepted, (cmd, endpoint) => {endpoint.execute(cmd); scalaz.Success(None)})_

  protected def forwardTrackedHandler = processRequest[TrackingTicket](Http_202_Accepted, (cmd, endpoint) => Some(endpoint.executeTracked(cmd)).success)_

  protected def forwardWithResultResponseHandler = withRequest((contentType, data, responder) =>
    (for {
      endpoint <- getEndpoint()
      command <- extractCommand(contentType, data)
    } yield (endpoint, command)).fold(
      prob => respondProblem(prob, Http_400_Bad_Request, contentType.channel, responder),
      {
        case (endpoint, command) =>
          endpoint.executeWithCallback(theAlmhirt.longDuration)(command, result => {
            result.fold(
              fail => {
                val (prob, status) = launderProblem(fail)
                respondProblem(prob, status, contentType.channel, responder)
              },
              succ => responseWorkflow(contentType.channel, succ, Http_200_OK, responder))
          })
      }))

  def forward(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardHandler(req, responder) }

  def forwardTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardTrackedHandler(req, responder) }

  def forwardWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardWithResultResponseHandler(req, responder) }
}