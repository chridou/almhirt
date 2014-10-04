package almhirt.configuration

import scala.concurrent.duration.FiniteDuration

sealed trait RetrySettings { def pause: FiniteDuration; def infiniteLoopPause: Option[FiniteDuration] }

final case class TimeLimitedRetrySettings(pause: FiniteDuration, maxTime: FiniteDuration, infiniteLoopPause: Option[FiniteDuration]) extends RetrySettings
final case class AttemptLimitedRetrySettings(pause: FiniteDuration, maxAttempts: Int, infiniteLoopPause: Option[FiniteDuration]) extends RetrySettings

