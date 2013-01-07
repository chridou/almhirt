package almhirt.ext.core.unfiltered.impl

import almhirt.common._
import almhirt.environment.Almhirt
import almhirt.commanding.DomainCommand
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered._
import unfiltered.request.HttpRequest
import unfiltered.netty.ReceivedMessage
import riftwarp.http.RiftWarpHttpFuns
import riftwarp.RiftWarp

class HttpCommandEndpoint(getEndpoint: () => AlmValidation[CommandEndpoint], getRiftWarp: () => AlmValidation[RiftWarp], theAlmhirt: Almhirt) extends ForwardsCommandsFromHttpRequest {
  protected def extractCommand(req: HttpRequest[ReceivedMessage]) =
    getRiftWarp().flatMap(riftWarp => UnfilteredFuns.transformContent[DomainCommand](req)(manifest[DomainCommand], riftWarp))

  def forward(req: HttpRequest[ReceivedMessage]) {
  }

  def forwardTracked(req: HttpRequest[ReceivedMessage]) {

  }

  def forwardWithResultResponse(req: HttpRequest[ReceivedMessage]) {

  }

}