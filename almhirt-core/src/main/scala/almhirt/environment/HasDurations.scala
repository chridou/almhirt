package almhirt.environment

import scala.concurrent.duration._

trait HasDurations {
  def durations: Durations
}

trait Durations {
  def shortDuration: FiniteDuration
  def mediumDuration: FiniteDuration
  def longDuration: FiniteDuration
  def extraLongDuration: FiniteDuration
}

trait DefaultDurations extends HasDurations {
  override val shortDuration = Duration(1, "s")
  override val mediumDuration = Duration(3, "s")
  override val longDuration = Duration(9, "s")
  override val extraLongDuration = Duration(22, "s")
}

object Durations {
  def apply(): HasDurations = new DefaultDurations {}
  def apply(short: FiniteDuration, medium: FiniteDuration, long: FiniteDuration, extraLong: FiniteDuration): Durations =
    new Durations {
      override val shortDuration = short
      override val mediumDuration = medium
      override val longDuration = long
      override val extraLongDuration = extraLong
    }
}