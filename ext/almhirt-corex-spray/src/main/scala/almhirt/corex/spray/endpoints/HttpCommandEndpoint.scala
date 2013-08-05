package almhirt.corex.spray.endpoints

import scala.language.postfixOps
import almhirt.common._
import almhirt.components.CommandEndpoint
import almhirt.commanding.ExecutionState
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

trait HttpCommandEndpoint extends HttpService {
  def endpoint: CommandEndpoint
  def maxSyncDuration: scala.concurrent.duration.FiniteDuration
  implicit def executionContext: scala.concurrent.ExecutionContext

  implicit def executionStateMarshaller: Marshaller[ExecutionState]
  implicit def problemMarshaller: Marshaller[Problem]
  implicit def commandUnmarshaller: Unmarshaller[Command]

  val execute = pathPrefix("execute") & (get & parameters('tracked ?, 'sync ?)) & entity(as[Command])

  val route = execute { (tracked, sync, cmd) =>
    ctx =>
      (tracked, sync) match {
        case (None, None) =>
          endpoint.execute(cmd)
          complete { (StatusCodes.Accepted, cmd.tryGetTrackingId.getOrElse("")) }
        case (Some(_), _) =>
          val trackId = endpoint.executeTracked(cmd)
          complete { (StatusCodes.Accepted, trackId) }
        case (_, Some(_)) =>
          endpoint.executeSync(cmd, maxSyncDuration).fold(
            prob =>
              ctx.complete(StatusCodes.InternalServerError, prob),
            res =>
              ctx.complete(StatusCodes.OK, res))
      }
  }
}