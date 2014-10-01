package almhirt.configuration

import scala.concurrent.duration.FiniteDuration

sealed trait RetrySettings { def pause: FiniteDuration }

final case class TimeLimitedRetrySettings(pause: FiniteDuration, maxTime: FiniteDuration) extends RetrySettings
final case class AttemptLimitedRetrySettings(pause: FiniteDuration, maxAttempts: Int) extends RetrySettings

