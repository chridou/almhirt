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
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.marshalling.Marshaller

trait HttpCommandEndpoint extends Directives {
  self: AlmHttpEndpoint with HasProblemMarshaller ⇒

  implicit def commandUnmarshaller: Unmarshaller[Command]
  implicit def commandResponseMarshaller: Marshaller[CommandResponse]

  def commandEndpoint: ActorRef
  def maxSyncDuration: scala.concurrent.duration.FiniteDuration
  implicit def executionContext: scala.concurrent.ExecutionContext
  val executeCommand = post & entity(as[Command])

  val executeCommandTerminator = pathPrefix("execute") {
    pathEnd {
      executeCommand { cmd ⇒
        implicit ctx ⇒ {
          ((commandEndpoint ? cmd)(maxSyncDuration)).successfulAlmFuture[CommandResponse].completeRequestPostMapped[CommandResponse] {
            case r: CommandAccepted ⇒ SuccessContent(r, StatusCodes.Accepted)
            case r: CommandNotAccepted ⇒ FailureContent(r, StatusCodes.TooManyRequests)
            case r: TrackedCommandResult ⇒ SuccessContent(r, StatusCodes.OK)
            case r: TrackedCommandTimedOut ⇒ FailureContent(r, StatusCodes.InternalServerError)
            case r: TrackerFailed ⇒ FailureContent(r, StatusCodes.InternalServerError)
          }
        }
      }
    }
  }
}