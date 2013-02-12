package almhirt.core

import scala.concurrent.duration._
import com.typesafe.config.Config
import almhirt.environment.configuration.ConfigHelper

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
  def apply(config: Config): HasDurations = new HasDurations { override val durations = Durations(config) }
  def apply(theDurations: Durations): HasDurations = new HasDurations { override val durations = theDurations }
  def apply(short: FiniteDuration, medium: FiniteDuration, long: FiniteDuration, extraLong: FiniteDuration): HasDurations =
    apply(Durations(short: FiniteDuration, medium: FiniteDuration, long: FiniteDuration, extraLong: FiniteDuration))

}

trait Durations {
  def shortDuration: FiniteDuration
  def mediumDuration: FiniteDuration
  def longDuration: FiniteDuration
  def extraLongDuration: FiniteDuration
  
  override def toString(): String =
    s"short: ${shortDuration.toString}, medium: ${mediumDuration.toString}, long: ${longDuration.toString}, extraLong: ${extraLongDuration.toString}"
}

trait DefaultDurations extends Durations {
  override val shortDuration = Duration(1, "s")
  override val mediumDuration = Duration(3, "s")
  override val longDuration = Duration(9, "s")
  override val extraLongDuration = Duration(22, "s")
}

object Durations {
  def apply(): Durations = new DefaultDurations {}
  def apply(config: Config): Durations = {
    val shortDuration = ConfigHelper.getMilliseconds(config)("almhirt.durations.short").getOrElse(Duration(1, "s"))
    val mediumDuration = ConfigHelper.getMilliseconds(config)("almhirt.durations.medium").getOrElse(Duration(3, "s"))
    val longDuration = ConfigHelper.getMilliseconds(config)("almhirt.durations.long").getOrElse(Duration(9, "s"))
    val extraLongDuration = ConfigHelper.getMilliseconds(config)("almhirt.durations.extralong").getOrElse(Duration(22, "s"))
    apply(shortDuration, mediumDuration, longDuration, extraLongDuration)
  }

  def apply(short: FiniteDuration, medium: FiniteDuration, long: FiniteDuration, extraLong: FiniteDuration): Durations =
    new Durations {
      override val shortDuration = short
      override val mediumDuration = medium
      override val longDuration = long
      override val extraLongDuration = extraLong
    }
}