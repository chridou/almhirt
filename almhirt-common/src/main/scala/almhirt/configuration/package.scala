package almhirt

import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import com.typesafe.config.Config

package object configuration {
  implicit class ConfigOps(self: Config) {
    def value[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[T] =
      configExtractor.getValue(self, path)

    def optValue[T](path: String)(implicit configExtractor: ConfigExtractor[T]): AlmValidation[Option[T]] =
      configExtractor.tryGetValue(self, path)

    def unsafeValue[T](path: String)(implicit configExtractor: ConfigExtractor[T]): Option[T] =
      configExtractor.tryGetValue(self, path).fold(
        fail ⇒ None,
        succ ⇒ succ)

    /**
     * The value is a None, if and only if it is the case insensitive String "None".
     *  Otherwise it is parsed with the given Extractor.
     */
    def magicOption[T: ConfigExtractor](path: String): AlmValidation[Option[T]] =
      ConfigStringExtractorInst.getValue(self, path).fold(
        problem => {
          self.value[T](path).map(Some(_))
        },
        mayBeMagicValue => {
          mayBeMagicValue.toLowerCase() match {
            case "none" => scalaz.Success(None)
            case _ => self.value[T](path).map(Some(_))
          }
        })

    def magicDefault[T: ConfigExtractor](defaultMarker: String, default: T)(path: String): AlmValidation[T] =
      ConfigStringExtractorInst.getValue(self, path).fold(
        problem => {
          self.value[T](path)
        },
        mayBeMagicDefault => {
          if (mayBeMagicDefault.toLowerCase() == defaultMarker)
            scalaz.Success(default)
          else
            self.value[T](path)
        })

    final def v[T: ConfigExtractor](path: String): AlmValidation[T] =
      value[T](path)

    final def opt[T: ConfigExtractor](path: String): AlmValidation[Option[T]] =
      optValue[T](path)

    final def unsafeOpt[T: ConfigExtractor](path: String): Option[T] =
      unsafeValue[T](path)

  }

  implicit val ConfigStringExtractorInst = new ConfigStringExtractor {}
  implicit val ConfigBooleanExtractorInst = new ConfigBooleanExtractor {}
  implicit val ConfigIntExtractorInst = new ConfigIntExtractor {}
  implicit val ConfigLongExtractorInst = new ConfigLongExtractor {}
  implicit val ConfigDoubleExtractorInst = new ConfigDoubleExtractor {}
  implicit val ConfigFiniteDurationMsExtractorInst = new ConfigFiniteDurationMsExtractor {}
  implicit val ConfigJodaDurationMsExtractorInst = new ConfigJodaDurationMsExtractor {}
  implicit val ConfigUuidExtractorInst = new ConfigUuidExtractor {}
  implicit val ConfigUriExtractorInst = new ConfigUriExtractor {}

  implicit val ConfigStringListExtractorInst = new ConfigStringListExtractor {}
  implicit val ConfigBooleanListExtractorInst = new ConfigBooleanListExtractor {}
  implicit val ConfigIntListExtractorInst = new ConfigIntListExtractor {}
  implicit val ConfigLongListExtractorInst = new ConfigLongListExtractor {}
  implicit val ConfigDoubleListExtractorInst = new ConfigDoubleListExtractor {}
  implicit val ConfigFiniteDurationListMsExtractorInst = new ConfigFiniteDurationListMsExtractor {}
  implicit val ConfigJodaDurationListMsExtractorInst = new ConfigJodaDurationListMsExtractor {}
  implicit val ConfigUuidListExtractorInst = new ConfigUuidListExtractor {}
  implicit val ConfigUriListExtractorInst = new ConfigUriListExtractor {}
  implicit val ConfigConfigListExtractorExtractorInst = new ConfigConfigListExtractor {}

  implicit val ConfigConfigExtractorInst = new ConfigConfigExtractor {}
  implicit val ConfigJavaPropertiesExtractorInst = new ConfigJavaPropertiesExtractor {}

  implicit object ImportanceConfigExtractor extends ConfigExtractor[Importance] {
    def getValue(config: Config, path: String): AlmValidation[Importance] =
      for {
        str <- config.v[String](path)
        importance <- Importance.fromString(str)
      } yield importance

    def tryGetValue(config: Config, path: String): AlmValidation[Option[Importance]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }
  }

  implicit object SeverityConfigExtractor extends ConfigExtractor[almhirt.problem.Severity] {
    def getValue(config: Config, path: String): AlmValidation[almhirt.problem.Severity] =
      for {
        str <- config.v[String](path)
        severity <- almhirt.problem.Severity.fromString(str)
      } yield severity

    def tryGetValue(config: Config, path: String): AlmValidation[Option[almhirt.problem.Severity]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }
  }
  
  implicit object ExecutionContextConfigExtractor extends ConfigExtractor[ExecutionContextSelector] {
    def getValue(config: Config, path: String): AlmValidation[ExecutionContextSelector] =
      for {
        str <- config.v[String](path)
        selector <- ExecutionContextSelector.parseString(str)
      } yield selector

    def tryGetValue(config: Config, path: String): AlmValidation[Option[ExecutionContextSelector]] =
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
        infiniteLoopPause <- section.magicOption[FiniteDuration]("retry-infinite-loop-pause")
        res <- build(pause, mode, maxTime, maxAttempts, infiniteLoopPause)
      } yield res
    }

    def tryGetValue(config: Config, path: String): AlmValidation[Option[RetrySettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }

    private def build(pause: FiniteDuration, mode: Option[String], maxTime: Option[FiniteDuration], maxAttempts: Option[Int], infiniteLoopPause: Option[FiniteDuration]): AlmValidation[RetrySettings] = {
      (mode, maxTime, maxAttempts) match {
        case (None, None, Some(ma)) =>
          AttemptLimitedRetrySettings(pause = pause, maxAttempts = ma, infiniteLoopPause = infiniteLoopPause).success
        case (None, Some(mt), None) =>
          TimeLimitedRetrySettings(pause = pause, maxTime = mt, infiniteLoopPause = infiniteLoopPause).success
        case (Some("retry-limit-attempts"), _, Some(ma)) =>
          AttemptLimitedRetrySettings(pause = pause, maxAttempts = ma, infiniteLoopPause = infiniteLoopPause).success
        case (Some("retry-limit-time"), Some(mt), _) =>
          TimeLimitedRetrySettings(pause = pause, maxTime = mt, infiniteLoopPause = infiniteLoopPause).success
        case (None, Some(mt), Some(ma)) =>
          UnspecifiedProblem("""When "retry-max-time" and "retry-max-attempts" are both set, you must specify the mode via "retry-mode"("retry-limit-time" | "retry-limit-attempts").""").failure
        case x =>
          UnspecifiedProblem("""Invalid retry settings: $x.""").failure
      }
    }
  }

}