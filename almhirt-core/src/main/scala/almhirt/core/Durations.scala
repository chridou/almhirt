package almhirt.core

import scala.concurrent.duration._
import almhirt.common._
import com.typesafe.config.Config

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
  def apply(config: Config): AlmValidation[Durations] = {
    import almhirt.configuration._
    for {
      shortDur <- config.v[FiniteDuration]("short")
      mediumDur <- config.v[FiniteDuration]("medium")
      longDur <- config.v[FiniteDuration]("long")
      extraLongDur <- config.v[FiniteDuration]("extralong")
    } yield apply(shortDur, mediumDur, longDur, extraLongDur)
  }

  def apply(short: FiniteDuration, medium: FiniteDuration, long: FiniteDuration, extraLong: FiniteDuration): Durations =
    new Durations {
      override val shortDuration = short
      override val mediumDuration = medium
      override val longDuration = long
      override val extraLongDuration = extraLong
    }
}