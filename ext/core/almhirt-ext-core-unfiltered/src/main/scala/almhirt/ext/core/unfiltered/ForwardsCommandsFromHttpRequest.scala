package almhirt.ext.core.unfiltered

import unfiltered.request.HttpRequest

trait ForwardsCommandsFromHttpRequest {
  def forward(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]): Unit
  def forwardTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any])
  def forwardWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]): Unit
}
