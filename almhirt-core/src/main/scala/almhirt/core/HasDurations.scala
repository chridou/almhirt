package almhirt.core

import almhirt.common._
import scala.concurrent.duration._
import com.typesafe.config.Config

trait HasDurations {
  def durations: Durations
  def defaultDuration = durations.mediumDuration
}

trait HasDefaultDurations extends HasDurations{
  override def durations = Durations()
  override def defaultDuration = durations.mediumDuration
}

object HasDurations {
  def apply(): HasDurations = new HasDurations { override val durations = Durations() }
  def apply(config: Config): AlmValidation[HasDurations] = Durations(config).map(theDurations => new HasDurations { override val durations = theDurations })
  def apply(theDurations: Durations): HasDurations = new HasDurations { override val durations = theDurations }
  def apply(short: FiniteDuration, medium: FiniteDuration, long: FiniteDuration, extraLong: FiniteDuration): HasDurations =
    apply(Durations(short: FiniteDuration, medium: FiniteDuration, long: FiniteDuration, extraLong: FiniteDuration))

}


