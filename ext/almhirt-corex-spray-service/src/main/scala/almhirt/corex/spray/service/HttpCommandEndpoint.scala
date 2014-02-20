package almhirt.corex.spray.service

import scala.language.postfixOps
import scalaz._, Scalaz._
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
  val executeCommand = (put & parameters('sync.as[Boolean] ?, 'ensure_tracking.as[Boolean] ?)) & entity(as[Command])
  val executeCommands = (put & parameters('sync.as[Boolean] ?, 'ensure_tracking.as[Boolean] ?)) & entity(as[Seq[DomainCommand]])

  val executeCommandTerminator = pathPrefix("execute") {
    pathEnd {
      executeCommand { (sync, ensure_tracking, cmd) =>
        ctx => {
          val effSync = sync | false
          val effTrack = (ensure_tracking | false)
          (effTrack, effSync) match {
            case (false, false) =>
              endpoint.execute(cmd)
              ctx.complete(StatusCodes.Accepted, "")
            case (true, false) =>
              val trackId = endpoint.executeTracked(cmd)
              ctx.complete(StatusCodes.Accepted, trackId)
            case (_, true) =>
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
    } ~ path("sequence") {
      executeCommands { (sync, ensure_tracking, cmds) =>
        ctx => {
          val effSync = sync | false
          val effTrack = (ensure_tracking | false)
          (effTrack, effSync) match {
            case (false, false) =>
              endpoint.executeDomainCommandSequence(cmds)
              ctx.complete(StatusCodes.Accepted, "")
            case (true, false) =>
              val trackId = endpoint.executeDomainCommandSequenceTracked(cmds)
              ctx.complete(StatusCodes.Accepted, trackId)
            case (_, true) =>
              endpoint.executeDomainCommandSequenceSync(cmds, maxSyncDuration).onComplete(
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
  }
}