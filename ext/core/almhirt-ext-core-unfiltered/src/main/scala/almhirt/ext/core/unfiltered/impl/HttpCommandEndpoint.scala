package almhirt.ext.core.unfiltered.impl

import almhirt.common._
import almhirt.environment.Almhirt
import almhirt.commanding.DomainCommand
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered._
import almhirt.http.impl.JustForTestingProblemLaundry
import almhirt.http._
import riftwarp._
import riftwarp.http.RiftWarpHttpFuns
import unfiltered.request._
import unfiltered.response._

class HttpCommandEndpoint(getEndpoint: () => AlmValidation[CommandEndpoint], riftWarp: RiftWarp, theAlmhirt: Almhirt) extends ForwardsCommandsFromHttpRequest {
  private val nice = true
  protected def launderProblem = JustForTestingProblemLaundry
  protected def respondProblem(prob: Problem, errorCode: HttpError, channel: RiftChannel with RiftHttpChannel, responder: unfiltered.Async.Responder[Any]) =
    UnfilteredFuns.respondProblem(riftWarp)(theAlmhirt.reportProblem)(nice)(prob, errorCode, channel, responder)

  protected def extractCommand(channel: RiftChannel with RiftHttpChannel, typeDescriptor: Option[TypeDescriptor], data: RiftDimension with RiftHttpDimension) =
    RiftWarpHttpFuns.transformIncomingContent[DomainCommand](riftWarp)(channel, typeDescriptor, data)
  
  protected def withRequest = UnfilteredFuns.withRequest(riftWarp)(nice)(launderProblem)(theAlmhirt.reportProblem)_

  protected def forwardHandler = withRequest((channel, typeDescriptor, data, responder) =>
    (for {
      endpoint <- getEndpoint()
      command <- extractCommand(channel, typeDescriptor, data)
    } yield (endpoint, command)).fold(
      prob => respondProblem(prob, Http_400_Bad_Request, channel, responder),
      {
        case (endpoint, command) =>
          endpoint.execute(command)
          responder.respond(Accepted ~> NoContent)
      }))

  def forward(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) { forwardHandler(req, responder) }

  def forwardTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    (  for {
        reqContentType <- UnfilteredFuns.getContentType(req)
        (channel, typeDescriptor) <- RiftWarpHttpFuns.extractChannelAndTypeDescriptor(reqContentType)
        endpoint <- getEndpoint()
        command <- extractCommand(req, channel)
      } yield (endpoint, command, channel)).fold(
      prob => (),
      endpointAndCmd => {
        endpointAndCmd._1.execute(endpointAndCmd._2)
        responder.respond(Accepted ~> NoContent)})
  }

  def forwardWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {

  }

}