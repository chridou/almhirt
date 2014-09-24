package almhirt.akkax

import akka.actor.{ Props, ActorRef, ActorPath, ActorSelection }
import almhirt.common._
import almhirt.tracking.CorrelationId

object ActorMessages {
  final case class CreateChildActor(props: Props, name: Option[String], returnActorRef: Boolean)
  sealed trait CreateChildActorRsp
  final case class ChildActorCreated(actorRef: ActorRef) extends CreateChildActorRsp
  final case class CreateChildActorFailed(cause: Problem) extends CreateChildActorRsp
  
  sealed trait ResovleResponse
  sealed trait ResolveSingleResponse extends ResovleResponse
  final case class ResolvedSingle(resolved: ActorRef, correlationId: Option[CorrelationId]) extends ResolveSingleResponse
  final case class SingleNotResolved(problem: Problem, correlationId: Option[CorrelationId]) extends ResolveSingleResponse

  sealed trait ResolveManyResponse extends ResovleResponse
  final case class ManyResolved(resolved: Map[String, ActorRef], correlationId: Option[CorrelationId]) extends ResolveManyResponse
  final case class ManyNotResolved(problem: Problem, correlationId: Option[CorrelationId]) extends ResolveManyResponse

}

object CreateChildActorHelper {
  import scala.concurrent.ExecutionContext
  import scala.concurrent.duration.FiniteDuration
  import akka.pattern._
  import almhirt.almfuture.all._
  def createChildActor(parent: ActorRef, props: Props, name: Option[String])(maxDur: FiniteDuration)(implicit execCtx: ExecutionContext): AlmFuture[ActorRef] = {
    parent.ask(ActorMessages.CreateChildActor(props, name, true))(maxDur).successfulAlmFuture[ActorMessages.CreateChildActorRsp].mapV {
      case ActorMessages.ChildActorCreated(newChild) => scalaz.Success(newChild)
      case ActorMessages.CreateChildActorFailed(problem) => scalaz.Failure(problem)
    }
  }
}