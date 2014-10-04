package almhirt

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Promise
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor.{ ActorRef, Props, ActorContext }
import akka.event.LoggingAdapter
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

    def retry[T](
      toTry: () => AlmFuture[T],
      onSuccess: T => Unit,
      onFailedAttempt: (FiniteDuration, Int, Problem) => Unit,
      onFailedLoop: (FiniteDuration, Int, Problem) => Unit,
      onFinalFailure: (FiniteDuration, Int, Problem) => Unit,
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]) {
      actorName match {
        case Some(name) =>
          self.actorOf(RetryActor.props(settings, toTry, onSuccess, onFailedAttempt, onFailedLoop, onFinalFailure), name)
        case None =>
          self.actorOf(RetryActor.props(settings, toTry, onSuccess, onFailedAttempt, onFailedLoop, onFinalFailure))
      }
    }

    def retryWithLogging[T](
      retryContext: String,
      toTry: () => AlmFuture[T],
      onSuccess: T => Unit,
      onFinalFailure: (FiniteDuration, Int, Problem) => Unit,
      log: LoggingAdapter,
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]) {
      retry(
        toTry,
        onSuccess,
        (t, n, p) => log.info(s"""		|$retryContext
        								|Failed after $n attempts and ${t.defaultUnitString}
        								|The problem was:
        								|$p""".stripMargin),
        (t, n, p) => log.warning(s"""	|$retryContext
        								|Failed after $n attempts and ${t.defaultUnitString}
        								|Will pause for ${settings.infiniteLoopPause.map(_.defaultUnitString).getOrElse("?")}
        								|The problem was:
        								|$p""".stripMargin),
        onFinalFailure,
        settings,
        actorName)
    }

    def retryNoReports[T](
      toTry: () => AlmFuture[T],
      onSuccess: T => Unit,
      onFinalFailure: (FiniteDuration, Int, Problem) => Unit,
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]) {
      retry(toTry, onSuccess, (_, _, _) => (), (_, _, _) => (), onFinalFailure, settings, actorName)
    }

    def retrySimple[T](
      toTry: () => AlmFuture[T],
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]): AlmFuture[T] = {
      val p = Promise[AlmValidation[T]]

      def handleFailure(elapsed: FiniteDuration, attempts: Int, problem: Problem) {
        val prob = UnspecifiedProblem(s"Retry failed after $attempts attempts and ${elapsed.defaultUnitString}: ${problem.message}", cause = Some(problem))
        p.success(scalaz.Failure(prob))
      }

      self.retryNoReports(toTry, (res: T) => p.success(scalaz.Success(res)), handleFailure, settings, actorName)

      new AlmFuture(p.future)
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