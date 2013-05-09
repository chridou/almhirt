package almhirt.ext.core.unfiltered

import almhirt.common._
import almhirt.util.http.HttpCommandEndpoint
import almhirt.http.ResponseConsumer
import almhirt.http.WorklowTerminatators
import unfiltered.request.HttpRequest

class UnfilteredHttpCommandEndpoint(httpCommandEndpoint: HttpCommandEndpoint[HttpRequest[Any]], responder: ResponseConsumer[unfiltered.Async.Responder[Any]], hec: HasExecutionContext, problemConsumer: Consumer[Problem]) {
  private val executeWorkflow = WorklowTerminatators.finishTerminal((in: HttpRequest[Any]) => httpCommandEndpoint.execute(in))(responder)
  private val executeTrackedWorkflow = WorklowTerminatators.finishTerminal((in: HttpRequest[Any]) => httpCommandEndpoint.executeTracked(in))(responder)
  private val executeWithResultWorkflow = WorklowTerminatators.finishTerminalF((in: HttpRequest[Any]) => httpCommandEndpoint.executeWithResult(in))(responder, problemConsumer, hec)

  def execute(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    hec.execute(executeWorkflow(req, responder))
  }

  def executeTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    hec.execute(executeTrackedWorkflow(req, responder))
  }

  def executeWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    hec.execute(executeWithResultWorkflow(req, responder))
  }
}
