package almhirt.corex.spray.service

import scala.language.postfixOps
import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.tracking._
import almhirt.httpx.spray.marshalling._
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.akkax._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.marshalling.Marshaller

object HttpCommandEndpoint {
  protected trait HttpCommandEndpointParams {
    def commandEndpoint: ActorRef
    def maxSyncDuration: scala.concurrent.duration.FiniteDuration
    def exectionContextSelector: ExtendedExecutionContextSelector
    implicit def commandUnmarshaller: Unmarshaller[Command]
    implicit def commandResponseMarshaller: Marshaller[CommandResponse]
  }
}

trait HttpCommandEndpoint extends Directives {
  me: Actor with AlmHttpEndpoint with HasProblemMarshaller with HasExecutionContexts ⇒

  def httpCommandEndpointParams: HttpCommandEndpoint.HttpCommandEndpointParams

  implicit private lazy val execCtx = httpCommandEndpointParams.exectionContextSelector.select(me, me.context)
  implicit private val commandUnmarshaller = httpCommandEndpointParams.commandUnmarshaller
  implicit private val commandResponseMarshaller = httpCommandEndpointParams.commandResponseMarshaller

  val executeCommand = post & entity(as[Command])

  val executeCommandTerminator = pathPrefix("execute") {
    pathEnd {
      executeCommand { cmd ⇒
        implicit ctx ⇒ {
          ((httpCommandEndpointParams.commandEndpoint ? cmd)(httpCommandEndpointParams.maxSyncDuration)).mapCastTo[CommandResponse].completeRequestPostMapped[CommandResponse] {
            case r: CommandAccepted ⇒ SuccessContent(r, StatusCodes.Accepted)
            case r: CommandNotAccepted ⇒
              r.why match {
                case _: RejectionReason.TooBusy => FailureContent(r, StatusCodes.TooManyRequests)
                case _: RejectionReason.NotReady => FailureContent(r, StatusCodes.ServiceUnavailable)
                case _: RejectionReason.AProblem => FailureContent(r, StatusCodes.InternalServerError)
              }
            case r: TrackedCommandResult ⇒ SuccessContent(r, StatusCodes.OK)
            case r: TrackedCommandTimedOut ⇒ FailureContent(r, StatusCodes.InternalServerError)
            case r: TrackerFailed ⇒ FailureContent(r, StatusCodes.InternalServerError)
          }
        }
      }
    }
  }
}