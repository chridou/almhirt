package almhirt.core.types

import scalaz._, Scalaz._

sealed trait BoilerPressure
case object NormalPressure extends BoilerPressure
case object HighPressure extends BoilerPressure
case object BoilerExplosionImminent extends BoilerPressure