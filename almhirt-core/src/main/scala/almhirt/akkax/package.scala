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
        maxResolveTime <- section.v[FiniteDuration]("max-resolve-time")
        resolveWait <- section.v[FiniteDuration]("resolve-wait")
        resolveInterval <- section.v[FiniteDuration]("resolve-pause")
      } yield ResolveSettings(maxResolveTime = maxResolveTime, resolveWait = resolveInterval, resolvePause = resolveInterval)

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

  implicit object RetrySettingsConfigExtractor extends ConfigExtractor[RetrySettings] {

    def getValue(config: Config, path: String): AlmValidation[RetrySettings] = {
      for {
        section <- config.v[Config](path)
        mode <- section.opt[String]("retry-mode")
        pause <- section.v[FiniteDuration]("retry-pause")
        maxTime <- section.opt[FiniteDuration]("retry-max-time")
        maxAttempts <- section.opt[Int]("retry-max-attempts")
        res <- build(pause, mode, maxTime, maxAttempts)
      } yield res
    }

    def tryGetValue(config: Config, path: String): AlmValidation[Option[RetrySettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }

    private def build(pause: FiniteDuration, mode: Option[String], maxTime: Option[FiniteDuration], maxAttempts: Option[Int]): AlmValidation[RetrySettings] = {
      (mode, maxTime, maxAttempts) match {
        case (None, None, Some(ma)) =>
          AttemptLimitedRetrySettings(pause = pause, maxAttempts = ma).success
        case (None, Some(mt), None) =>
          TimeLimitedRetrySettings(pause = pause, maxTime = mt).success
        case (Some("retry-limit-attempts"), _, Some(ma)) =>
          AttemptLimitedRetrySettings(pause = pause, maxAttempts = ma).success
        case (Some("retry-limit-time"), Some(mt), _) =>
          TimeLimitedRetrySettings(pause = pause, maxTime = mt).success
        case (None, Some(mt), Some(ma)) =>
          UnspecifiedProblem("""When "retry-max-time" and "retry-max-attempts" are both set, you must specify the mode via "retry-mode"("retry-limit-time" | "retry-limit-attempts").""").failure
        case x =>
          UnspecifiedProblem("""Invalid retry settings: $x.""").failure
      }
    }

  }
}