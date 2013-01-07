package almhirt.ext.core.unfiltered

import unfiltered.request.HttpRequest
import unfiltered.netty.ReceivedMessage

trait ForwardsCommandsFromHttpRequest {
  def forward(req: HttpRequest[ReceivedMessage]): Unit
  def forwardTracked(req: HttpRequest[ReceivedMessage])
  def forwardWithResultResponse(req: HttpRequest[ReceivedMessage]): Unit
}
