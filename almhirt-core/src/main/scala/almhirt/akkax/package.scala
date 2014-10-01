package almhirt

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
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
        case Some(name) ⇒
          self.actorOf(SingleResolver.props(toResolve, settings, correlationId), name)
        case None ⇒
          self.actorOf(SingleResolver.props(toResolve, settings, correlationId))
      }
    }
    def resolveMany(toResolve: Map[String, ToResolve], settings: ResolveSettings, correlationId: Option[CorrelationId], resolverName: Option[String] = None) = {
      resolverName match {
        case Some(name) ⇒
          self.actorOf(MultiResolver.props(toResolve, settings, correlationId), name)
        case None ⇒
          self.actorOf(MultiResolver.props(toResolve, settings, correlationId))
      }
    }

    def childFrom(factory: ComponentFactory): AlmValidation[ActorRef] = factory(self)
  }

  import almhirt.configuration._
  import com.typesafe.config.Config
  implicit object ResolveSettingsConfigExtractor extends ConfigExtractor[ResolveSettings] {
    def getValue(config: Config, path: String): AlmValidation[ResolveSettings] =
      for {
        section <- config.v[Config](path)
        resolveWait <- section.v[FiniteDuration]("resolve-wait")
        retrySettings <- config.v[RetrySettings](path)
      } yield ResolveSettings(retrySettings = retrySettings, resolveWait = resolveWait)

    def tryGetValue(config: Config, path: String): AlmValidation[Option[ResolveSettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }
  }

  implicit object ExtendedExecutionContextSelectorConfigExtractor extends ConfigExtractor[ExtendedExecutionContextSelector] {
    def getValue(config: Config, path: String): AlmValidation[ExtendedExecutionContextSelector] =
      for {
        str <- config.v[String](path)
        selector <- ExtendedExecutionContextSelector.parseString(str)
      } yield selector

    def tryGetValue(config: Config, path: String): AlmValidation[Option[ExtendedExecutionContextSelector]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }

  }

}