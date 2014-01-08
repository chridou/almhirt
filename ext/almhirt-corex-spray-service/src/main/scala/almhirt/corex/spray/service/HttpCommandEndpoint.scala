package almhirt.corex.spray.service

import scala.language.postfixOps
import almhirt.common._
import almhirt.core.types._
import almhirt.components.CommandEndpoint
import almhirt.components.ExecutionStateTracker._
import almhirt.commanding._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import almhirt.httpx.spray.marshalling._
import almhirt.corex.spray.marshalling.HasCoreMarshallers

trait HttpCommandEndpoint extends Directives { self: HasCommonMarshallers with HasCommonUnmarshallers with HasCoreMarshallers =>

  def endpoint: CommandEndpoint
  def maxSyncDuration: scala.concurrent.duration.FiniteDuration
  implicit def executionContext: scala.concurrent.ExecutionContext
  val executeCommand = (put & parameters('tracked ?, 'sync ?)) & entity(as[Command])

  val executeCommandTerminator = path("execute") {
    executeCommand { (tracked, sync, cmd) =>
      ctx =>
        (tracked, sync) match {
          case (None, None) =>
            endpoint.execute(cmd)
            ctx.complete(StatusCodes.Accepted, cmd.tryGetTrackingId.getOrElse(""))
          case (Some(_), _) =>
            val trackId = endpoint.executeTracked(cmd)
            ctx.complete(StatusCodes.Accepted, trackId)
          case (_, Some(_)) =>
            endpoint.executeSync(cmd, maxSyncDuration).onComplete(
              prob =>
                prob match {
                  case OperationTimedOutProblem(p) => ctx.complete(StatusCodes.InternalServerError, prob)
                  case p => ctx.complete(StatusCodes.InternalServerError, prob)
                },
              res => {
                res match {
                  case FinishedExecutionStateResult(s: ExecutionSuccessful) => ctx.complete(StatusCodes.OK, s)
                  case FinishedExecutionStateResult(s: ExecutionFailed) => ctx.complete(StatusCodes.InternalServerError, s)
                  case ExecutionStateTrackingFailed(_, prob) => ctx.complete(StatusCodes.InternalServerError, prob)
                }
              })
        }
    }
  }
}