package almhirt.util.http

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.http._
import almhirt.core.Almhirt
import almhirt.util.CommandEndpoint
import almhirt.util.TrackingTicket
import almhirt.util.ResultOperationState
import almhirt.util.CommandWithMaxResponseDuration

class HttpCommandEndpoint[TIn](
  commandEndpoint: CommandEndpoint)(implicit
  commandUnmarshaller: HttpUnmarshaller[Command],
  ticketMarshaller: HttpMarshaller[TrackingTicket],
  commandWithMaxDurationUnarshaller: HttpUnmarshaller[CommandWithMaxResponseDuration],
  resultMarshaller: HttpMarshaller[ResultOperationState],
  instances: HttpInstances,
  requestExtractor: HttpRequestExtractor[TIn],
  problemConsumer: Consumer[Problem],
  hasExecutionContext: HasExecutionContext) {


  private val executeFlow = RequestAcceptWorkflowBlox.toResponse[TIn, Command](cmd => commandEndpoint.execute(cmd), Http_202_Accepted)
  private val executeTrackedFlow = RequestResponseWorkflowBlox.toResponse[TIn, Command, TrackingTicket](cmd => commandEndpoint.executeTracked(cmd).success, Http_202_Accepted)
  private val executeWithResultFlow = RequestResponseWorkflowBlox.toResponseF[TIn, CommandWithMaxResponseDuration, ResultOperationState](cmd => commandEndpoint.executeWithResult(cmd), Http_200_OK)

  def execute(from: TIn): HttpResponse = executeFlow(from)
  def executeTracked(from: TIn): HttpResponse = executeTrackedFlow(from)
  def executeWithResult(from: TIn): AlmFuture[HttpResponse] = executeWithResultFlow(from)

}