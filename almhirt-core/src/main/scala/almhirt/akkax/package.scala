package almhirt

import akka.actor.{ ActorRef, Props, ActorContext }
import almhirt.tracking.CorrelationId
import almhirt.akkax.SingleResolver

package object akkax {

  implicit class AkkaXActorContextOps(self: ActorContext) {
    import scala.concurrent.ExecutionContext
    import scala.concurrent.duration.FiniteDuration
    def resolveSingle(toResolve: ToResolve, settings: ResolveSettings, correlationId: Option[CorrelationId], resolverName: Option[String] = None) = {
      resolverName match {
        case Some(name) =>
          self.actorOf(SingleResolver.props(toResolve, settings, correlationId), name)
        case None =>
          self.actorOf(SingleResolver.props(toResolve, settings, correlationId))
      }
    }
    def resolveMany(toResolve: Map[String, ToResolve], settings: ResolveSettings, correlationId: Option[CorrelationId], resolverName: Option[String] = None) = {
      resolverName match {
        case Some(name) =>
          self.actorOf(MultiResolver.props(toResolve, settings, correlationId), name)
        case None =>
          self.actorOf(MultiResolver.props(toResolve, settings, correlationId))
      }
    }
  }

  implicit class AkkaXActorRefOps(self: ActorRef) {
    import scala.concurrent.ExecutionContext
    import scala.concurrent.duration.FiniteDuration
    def createChildActor(props: Props, name: Option[String])(maxDur: FiniteDuration)(implicit execCtx: ExecutionContext) =
      CreateChildActorHelper.createChildActor(self, props, name)(maxDur)
  }
}