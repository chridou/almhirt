package almhirt.akkax

import akka.actor.{ Props, ActorRef, ActorPath, ActorSelection }
import almhirt.common._
import almhirt.tracking.CorrelationId

object ActorMessages {
  final case class CreateChildActors(factories: Seq[ComponentFactory], returnActorRefs: Boolean, correlationId: Option[CorrelationId])
  final case class CreateChildActor(factory: ComponentFactory, returnActorRef: Boolean, correlationId: Option[CorrelationId])
  sealed trait CreateChildActorRsp
  final case class ChildActorCreated(actorRef: ActorRef, correlationId: Option[CorrelationId]) extends CreateChildActorRsp
  final case class CreateChildActorFailed(cause: Problem, correlationId: Option[CorrelationId]) extends CreateChildActorRsp

  sealed trait ResovleResponse
  sealed trait ResolveSingleResponse extends ResovleResponse
  final case class ResolvedSingle(resolved: ActorRef, correlationId: Option[CorrelationId]) extends ResolveSingleResponse
  final case class SingleNotResolved(problem: Problem, correlationId: Option[CorrelationId]) extends ResolveSingleResponse

  sealed trait ResolveManyResponse extends ResovleResponse
  final case class ManyResolved(resolved: Map[String, ActorRef], correlationId: Option[CorrelationId]) extends ResolveManyResponse
  final case class ManyNotResolved(problem: Problem, correlationId: Option[CorrelationId]) extends ResolveManyResponse

  final case class UnfoldComponents(factories: Seq[ComponentFactory])

  sealed trait CircuitStateChangedMessage
  sealed trait CircuitNotAllWillFail extends CircuitStateChangedMessage
  sealed trait CircuitAllWillFail extends CircuitStateChangedMessage
  case object CircuitClosed extends CircuitNotAllWillFail
  case object CircuitHalfOpened extends CircuitNotAllWillFail
  case object CircuitOpened extends CircuitAllWillFail
  case object CircuitFuseRemoved extends CircuitAllWillFail
  case object CircuitDestroyed extends CircuitAllWillFail
  case object CircuitCircumvented extends CircuitNotAllWillFail
  
  sealed trait HerderAppStartupMessage
  case object HerderServiceAppStarted extends HerderAppStartupMessage
  final case class HerderServiceAppFailedToStart(problem: Problem) extends HerderAppStartupMessage
  
  sealed trait ComponentControlMessage
  case object Pause extends ComponentControlMessage
  case object Resume extends ComponentControlMessage
  case object Restart extends ComponentControlMessage
  case object ReportComponentState extends ComponentControlMessage
  
}

object CreateChildActorHelper {
  import scala.concurrent.ExecutionContext
  import scala.concurrent.duration.FiniteDuration
  import akka.pattern._
  import almhirt.almfuture.all._
  def createChildActor(parent: ActorRef, factory: ComponentFactory, correlationId: Option[CorrelationId])(maxDur: FiniteDuration)(implicit execCtx: ExecutionContext): AlmFuture[ActorRef] = {
    parent.ask(ActorMessages.CreateChildActor(factory, true, correlationId))(maxDur).mapCastTo[ActorMessages.CreateChildActorRsp].mapV {
      case ActorMessages.ChildActorCreated(newChild, correlationId) ⇒ scalaz.Success(newChild)
      case ActorMessages.CreateChildActorFailed(problem, correlationId) ⇒ scalaz.Failure(problem)
    }
  }
}