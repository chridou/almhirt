package almhirt

import akka.actor.{ ActorRef, Props }
package object akkax {

  implicit class AkkaXActorRefOps(self: ActorRef) {
    import scala.concurrent.ExecutionContext
    import scala.concurrent.duration.FiniteDuration
    def createChildActor(props: Props, name: Option[String])(maxDur: FiniteDuration)(implicit execCtx: ExecutionContext) =
      CreateChildActorHelper.createChildActor(self, props, name)(maxDur)
  }
}