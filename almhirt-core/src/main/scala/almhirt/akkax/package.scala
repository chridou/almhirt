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
import almhirt.akkax.CircuitStartState

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
      toTry: () ⇒ AlmFuture[T],
      onSuccess: T ⇒ Unit,
      onFailedAttempt: (FiniteDuration, Int, Problem) ⇒ Unit,
      onFailedLoop: (FiniteDuration, Int, Problem) ⇒ Unit,
      onFinalFailure: (FiniteDuration, Int, Problem) ⇒ Unit,
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]) {
      actorName match {
        case Some(name) ⇒
          self.actorOf(RetryActor.props(settings, toTry, onSuccess, onFailedAttempt, onFailedLoop, onFinalFailure), name)
        case None ⇒
          self.actorOf(RetryActor.props(settings, toTry, onSuccess, onFailedAttempt, onFailedLoop, onFinalFailure))
      }
    }

    def retryWithLogging[T](
      retryContext: String,
      toTry: () ⇒ AlmFuture[T],
      onSuccess: T ⇒ Unit,
      onFinalFailure: (FiniteDuration, Int, Problem) ⇒ Unit,
      log: LoggingAdapter,
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]) {
      retry(
        toTry,
        onSuccess,
        (t, n, p) ⇒ log.info(s"""		|$retryContext
        								|Failed after $n attempts and ${t.defaultUnitString}
        								|The problem was:
        								|$p""".stripMargin),
        (t, n, p) ⇒ log.warning(s"""	|$retryContext
        								|Failed after $n attempts and ${t.defaultUnitString}
        								|Will pause for ${settings.infiniteLoopPause.map(_.defaultUnitString).getOrElse("?")}
        								|The problem was:
        								|$p""".stripMargin),
        onFinalFailure,
        settings,
        actorName)
    }

    def retryNoReports[T](
      toTry: () ⇒ AlmFuture[T],
      onSuccess: T ⇒ Unit,
      onFinalFailure: (FiniteDuration, Int, Problem) ⇒ Unit,
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]) {
      retry(toTry, onSuccess, (_, _, _) ⇒ (), (_, _, _) ⇒ (), onFinalFailure, settings, actorName)
    }

    def retrySimple[T](
      toTry: () ⇒ AlmFuture[T],
      settings: almhirt.configuration.RetrySettings,
      actorName: Option[String]): AlmFuture[T] = {
      val p = Promise[AlmValidation[T]]

      def handleFailure(elapsed: FiniteDuration, attempts: Int, problem: Problem) {
        val prob = UnspecifiedProblem(s"Retry failed after $attempts attempts and ${elapsed.defaultUnitString}: ${problem.message}", cause = Some(problem))
        p.success(scalaz.Failure(prob))
      }

      self.retryNoReports(toTry, (res: T) ⇒ p.success(scalaz.Success(res)), handleFailure, settings, actorName)

      new AlmFuture(p.future)
    }

    def childFrom(factory: ComponentFactory): AlmValidation[ActorRef] = factory(self)
  }

  import almhirt.configuration._
  import com.typesafe.config.Config
  implicit object ResolveSettingsConfigExtractor extends ConfigExtractor[ResolveSettings] {
    def getValue(config: Config, path: String): AlmValidation[ResolveSettings] =
      for {
        section ← config.v[Config](path)
        resolveWait ← section.v[FiniteDuration]("resolve-wait")
        retrySettings ← config.v[RetrySettings](path)
      } yield ResolveSettings(retrySettings = retrySettings, resolveWait = resolveWait)

    def tryGetValue(config: Config, path: String): AlmValidation[Option[ResolveSettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit object CircuitStartStateConfigExtractor extends ConfigExtractor[CircuitStartState] {
    def getValue(config: Config, path: String): AlmValidation[CircuitStartState] =
      for {
        str ← config.v[String](path)
        startState ← CircuitStartState.parseString(str)
      } yield startState

    def tryGetValue(config: Config, path: String): AlmValidation[Option[CircuitStartState]] =
      config.opt[String](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }

  }

  implicit object ExtendedExecutionContextSelectorConfigExtractor extends ConfigExtractor[ExtendedExecutionContextSelector] {
    def getValue(config: Config, path: String): AlmValidation[ExtendedExecutionContextSelector] =
      for {
        str ← config.v[String](path)
        selector ← ExtendedExecutionContextSelector.parseString(str)
      } yield selector

    def tryGetValue(config: Config, path: String): AlmValidation[Option[ExtendedExecutionContextSelector]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }

  }

  implicit object AlmCircuitBreakerSettingsConfigExtractor extends ConfigExtractor[CircuitControlSettings] {
    def getValue(config: Config, path: String): AlmValidation[CircuitControlSettings] =
      for {
        section ← config.v[Config](path)
        maxFailures ← section.v[Int]("max-failures")
        failuresWarnThreshold ← section.magicOption[Int]("failures-warn-threshold")
        callTimeout ← section.v[FiniteDuration]("call-timeout")
        resetTimeout ← section.magicOption[FiniteDuration]("reset-timeout")
        startState ← section.opt[CircuitStartState]("start-state")
      } yield CircuitControlSettings(maxFailures, failuresWarnThreshold, callTimeout, resetTimeout, startState getOrElse CircuitStartState.Closed)

    def tryGetValue(config: Config, path: String): AlmValidation[Option[CircuitControlSettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit object XRetrySettingsConfigExtractor extends ConfigExtractor[XRetrySettings] {
    def getValue(config: Config, path: String): AlmValidation[XRetrySettings] = {
      for {
        section ← config.v[Config](path)
        numberOfRetries ← section.v[String]("number-of-retries")
        delayMode ← section.v[String]("delay-mode")
        importanceStrOpt ← section.magicOption[String]("importance")
        importanceOpt ← importanceStrOpt.map(Importance.fromString).validationOut
        contextStrOpt ← section.opt[String]("context-description")
        executorSelectorOpt ← section.opt[ExtendedExecutionContextSelector]("executor")
        res ← build(numberOfRetries, delayMode, importanceOpt, contextStrOpt, executorSelectorOpt)
      } yield res
    }

    def tryGetValue(config: Config, path: String): AlmValidation[Option[XRetrySettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }

    private def build(
      numberOfRetries: String,
      delayMode: String,
      importanceOpt: Option[Importance],
      contextStrOpt: Option[String],
      executorSelectorOpt: Option[ExtendedExecutionContextSelector]): AlmValidation[XRetrySettings] = {
      import almhirt.almvalidation.kit._
      import scalaz._, Scalaz._
      val norV =
        numberOfRetries.toLowerCase match {
          case "no-retry" ⇒
            NumberOfRetries.NoRetry.success
          case "0" ⇒
            NumberOfRetries.NoRetry.success
          case "infinite" ⇒
            NumberOfRetries.InfiniteRetries.success
          case "∞" ⇒
            NumberOfRetries.InfiniteRetries.success
          case x ⇒
            x.toIntAlm.flatMap(v ⇒
              if (v == 0)
                NumberOfRetries.NoRetry.success
              else if (v < 0)
                ConstraintViolatedProblem(s"number-of-retries may not be less than zero: $x").failure
              else
                NumberOfRetries.LimitedRetries(v).success)
        }
      val dmV =
        delayMode.toLowerCase match {
          case "no-delay" ⇒
            RetryDelayMode.NoDelay.success
          case x ⇒
            x.toDurationAlm.flatMap(dur ⇒
              if (dur == scala.concurrent.duration.Duration.Zero)
                RetryDelayMode.NoDelay.success
              else if (dur < scala.concurrent.duration.Duration.Zero)
                ConstraintViolatedProblem(s"delay-mode may not travel back in time: $x").failure
              else
                RetryDelayMode.ConstantDelay(dur).success)
        }

      val npParamsV =
        (importanceOpt, contextStrOpt) match {
          case (None, None)           ⇒ None.success
          case (Some(imp), None)      ⇒ Some(XRetrySettings.NotifyingParams(imp, None)).success
          case (Some(imp), Some(ctx)) ⇒ Some(XRetrySettings.NotifyingParams(imp, Some(ctx))).success
          case (None, Some(ctx))      ⇒ ConstraintViolatedProblem("""It makes no sense to specify "context-description" when there is no importance.""").failure
        }

      val ecs = executorSelectorOpt.success

      (norV.toAgg |@| dmV.toAgg |@| ecs.toAgg |@| npParamsV.toAgg)(XRetrySettings.apply).leftMap { p ⇒ ConfigurationProblem("Could not create XRetrySettings.", cause = Some(p)) }
    }
  }

  implicit object AkkaPeriodicSchedulingMagnet extends almhirt.tooling.PeriodicSchedulingMagnet[akka.actor.Scheduler] {
    def schedule(to: akka.actor.Scheduler, initialDelay: FiniteDuration, interval: FiniteDuration, action: () ⇒ Unit)(implicit executor: scala.concurrent.ExecutionContext): almhirt.common.Stoppable = {
      val cancellable = to.schedule(initialDelay, interval)(action())
      
      new almhirt.common.Stoppable {
        override def stop() {
          cancellable.cancel()
        }
      }
    }
  }

}