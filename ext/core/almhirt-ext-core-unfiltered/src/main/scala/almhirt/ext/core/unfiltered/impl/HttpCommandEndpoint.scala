package almhirt.ext.core.unfiltered.impl

import almhirt.common._
import almhirt.environment.Almhirt
import almhirt.commanding.DomainCommand
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered._
import riftwarp._
import riftwarp.http.RiftWarpHttpFuns
import unfiltered.request._
import unfiltered.response._
import almhirt.http.impl.JustForTestingProblemLaundry

class HttpCommandEndpoint(getEndpoint: () => AlmValidation[CommandEndpoint], riftWarp: RiftWarp, theAlmhirt: Almhirt) extends ForwardsCommandsFromHttpRequest {
  private val nice = true
  protected def launderProblem = JustForTestingProblemLaundry
  protected def extractCommand(req: HttpRequest[Any], channel: RiftChannel with RiftHttpChannel, typeDescriptor: Option[TypeDescriptor]) =
  	UnfilteredFuns.transformContent[DomainCommand](req, Some(channel), None)(manifest[DomainCommand], riftWarp)

//  val createWorkFlow = RiftWarpHttpFuns.createResponseWorkflow[Unit](riftWarp)(launderProblem)(theAlmhirt.reportProblem)(nice)_
//  val problemHandler = RiftWarpHttpFuns.createProblemHandler[Unit](riftWarp)(theAlmhirt.reportProblem)(nice)
  
  def forward(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {
    
//    (  for {
//        reqContentType <- UnfilteredFuns.getContentType(req)
//        (channel, typeDescriptor) <- RiftWarpHttpFuns.extractChannelAndTypeDescriptor(reqContentType)
//        endpoint <- getEndpoint()
//        command <- extractCommand(req, channel, typeDescriptor)
//      } yield (endpoint, command, channel)).fold(
//      prob => (),
//      endpointAndCmd => {
//        endpointAndCmd._1.execute(endpointAndCmd._2)
//        responder.respond(Accepted ~> NoContent)})
  }

  def forwardTracked(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {

  }

  def forwardWithResultResponse(req: HttpRequest[Any], responder: unfiltered.Async.Responder[Any]) {

  }

}