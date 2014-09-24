package almhirt.akkax

import akka.actor.{ Props, ActorRef }
import almhirt.common._

object ActorMessages {
  final case class CreateChildActor(props: Props, name: Option[String], returnActorRef: Boolean)
  sealed trait CreateChildActorRsp
  final case class ChildActorCreated(actorRef: ActorRef) extends CreateChildActorRsp
  final case class CreateChildActorFailed(cause: Problem) extends CreateChildActorRsp
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