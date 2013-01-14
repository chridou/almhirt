package almhirt.ext.core.unfiltered.impl

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
  private val nice = true
  protected def launderProblem = JustForTestingProblemLaundry
  protected def respondProblem(prob: Problem, errorCode: HttpError, channel: RiftHttpChannel, responder: unfiltered.Async.Responder[Any]) =
    UnfilteredFuns.respondProblem(riftWarp)(theAlmhirt.reportProblem)(nice)(prob, errorCode, channel, responder)

  protected def withRequest = UnfilteredFuns.withRequest(riftWarp)(nice)(launderProblem)(theAlmhirt.reportProblem)_
  protected val responseWorkflow = UnfilteredFuns.createResponseWorkflow(riftWarp)(launderProblem)(theAlmhirt.reportProblem)(nice)
  protected def extractCommand(contentType: HttpContentType, data: RiftDimension with RiftHttpDimension) =
    RiftWarpHttpFuns.transformIncomingContent[DomainCommand](riftWarp)(contentType, data)

  protected def forwardHandler = withRequest((contentType, data, responder) =>
    (for {
      endpoint <- getEndpoint()
      command <- extractCommand(contentType, data)
    } yield (endpoint, command)).fold(
      prob => respondProblem(prob, Http_400_Bad_Request, contentType.channel, responder),
      {
        case (endpoint, command) =>
          endpoint.execute(command)
          responder.respond(Accepted ~> NoContent)
      }))

  protected def forwardTrackedHandler = withRequest((contentType, data, responder) =>
    (for {
      endpoint <- getEndpoint()
      command <- extractCommand(contentType, data)
    } yield (endpoint, command)).fold(
      prob => respondProblem(prob, Http_400_Bad_Request, contentType.channel, responder),
      {
        case (endpoint, command) =>
          val ticket = endpoint.executeTracked(command)
          responseWorkflow(contentType.channel, ticket, Http_200_OK, responder)
      }))

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
                respondProblem(prob, status, contentType.channel, responder)},
              succ => responseWorkflow(contentType.channel, succ, Http_200_OK, responder))
          })
      }))

  def forward(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardHandler(req, responder) }

  def forwardTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardTrackedHandler(req, responder) }

  def forwardWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardWithResultResponseHandler(req, responder) }
}