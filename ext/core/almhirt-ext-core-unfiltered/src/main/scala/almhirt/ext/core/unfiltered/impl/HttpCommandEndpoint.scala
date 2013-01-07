package almhirt.ext.core.unfiltered.impl

import almhirt.common._
import almhirt.environment.Almhirt
import almhirt.commanding.DomainCommand
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered._
import unfiltered.request._
import unfiltered.response._
import riftwarp.http.RiftWarpHttpFuns
import riftwarp.RiftWarp

class HttpCommandEndpoint(getEndpoint: () => AlmValidation[CommandEndpoint], getRiftWarp: () => AlmValidation[RiftWarp], theAlmhirt: Almhirt) extends ForwardsCommandsFromHttpRequest {
  protected def extractCommand(req: HttpRequest[Any]) =
    getRiftWarp().flatMap(riftWarp => UnfilteredFuns.transformContent[DomainCommand](req)(manifest[DomainCommand], riftWarp))

  def forward(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    (  for {
        endpoint <- getEndpoint()
        command <- extractCommand(req)
      } yield (endpoint, command)).fold(
      prob => (),
      endpointAndCmd => {
        endpointAndCmd._1.execute(endpointAndCmd._2)
        responder.respond(Accepted ~> NoContent)})
  }

  def forwardTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {

  }

  def forwardWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {

  }

}