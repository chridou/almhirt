package almhirt.corex.spray.service

import scala.language.postfixOps
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.core.types._
import almhirt.components.CommandEndpoint
import almhirt.components.ExecutionStateTracker._
import almhirt.commanding._
import almhirt.httpx.spray.marshalling._
import almhirt.corex.spray.marshalling.{ HasCoreMarshallers, HasCoreUnmarshallers }
import almhirt.httpx.spray.service.AlmHttpEndpoint
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

trait HttpCommandEndpoint extends Directives {
  self: AlmHttpEndpoint with HasCommonMarshallers with HasCommonUnmarshallers with HasCoreMarshallers with HasCoreUnmarshallers =>

  def endpoint: CommandEndpoint
  def maxSyncDuration: scala.concurrent.duration.FiniteDuration
  implicit def executionContext: scala.concurrent.ExecutionContext
  val executeCommand = (post & parameters('sync.as[Boolean] ?, 'ensure_tracking.as[Boolean] ?)) & entity(as[Command])
  val executeCommands = (post & parameters('sync.as[Boolean] ?, 'ensure_tracking.as[Boolean] ?)) & entity(as[Seq[DomainCommand]])

  val executeCommandTerminator = pathPrefix("execute") {
    pathEnd {
      executeCommand { (sync, ensure_tracking, cmd) =>
        implicit ctx => {
          val effSync = sync | false
          val effTrack = (ensure_tracking | false)
          (effTrack, effSync) match {
            case (false, false) =>
              endpoint.execute(cmd)
              ctx.complete(StatusCodes.Accepted, "")
            case (true, false) =>
              endpoint.executeTracked(cmd).completeRequestAccepted
            case (_, true) =>
              endpoint.executeSync(cmd, maxSyncDuration).completeRequestPostMapped[ExecutionState] {
                case FinishedExecutionStateResult(s: ExecutionSuccessful) => SuccessContent(s)
                case FinishedExecutionStateResult(s: ExecutionFailed) => FailureContent(s)
                case ExecutionStateTrackingFailed(_, prob) => ProblemContent(prob)
              }
          }
        }
      }
    } ~ path("sequence") {
      executeCommands { (sync, ensure_tracking, cmds) =>
        implicit ctx => {
          val effSync = sync | false
          val effTrack = (ensure_tracking | false)
          (effTrack, effSync) match {
            case (false, false) =>
              endpoint.executeDomainCommandSequence(cmds)
              ctx.complete(StatusCodes.Accepted, "")
            case (true, false) =>
              endpoint.executeDomainCommandSequenceTracked(cmds).completeRequestAccepted
            case (_, true) =>
              endpoint.executeDomainCommandSequenceSync(cmds, maxSyncDuration).completeRequestPostMapped[ExecutionState] {
                case FinishedExecutionStateResult(s: ExecutionSuccessful) => SuccessContent(s)
                case FinishedExecutionStateResult(s: ExecutionFailed) => FailureContent(s)
                case ExecutionStateTrackingFailed(_, prob) => ProblemContent(prob)
              }
          }
        }
      }
    }
  }
}