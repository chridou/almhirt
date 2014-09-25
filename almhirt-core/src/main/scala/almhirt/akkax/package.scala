package almhirt

import scala.concurrent.duration.FiniteDuration
import akka.actor.{ ActorRef, Props, ActorContext }
import almhirt.common._
import almhirt.almvalidation.kit._
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
    
    def childFrom(factory: ComponentFactory): AlmValidation[ActorRef] = factory(self)
  }
 
  import almhirt.configuration._
  import com.typesafe.config.Config
  implicit object ResolveConfigExtractor extends ConfigExtractor[ResolveSettings] {
    def getValue(config: Config, path: String): AlmValidation[ResolveSettings] =
      for {
        section <- config.v[Config](path)
        maxResolveTime <- section.v[FiniteDuration]("max-resolve-time")
        resolveWait <- section.v[FiniteDuration]("resolve-wait")
        resolveInterval <- section.v[FiniteDuration]("resolve-pause")
      } yield ResolveSettings(maxResolveTime = maxResolveTime, resolveWait = resolveInterval, resolvePause = resolveInterval)

    def tryGetValue(config: Config, path: String): AlmValidation[Option[ResolveSettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) => getValue(config, path).map(Some(_))
        case None => scalaz.Success(None)
      }
  }
}