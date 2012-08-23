package almhirt.xtractnduce

import almhirt.validation._

trait CanXtract[T] {
  def xtract(xtractor: XTractor): AlmValidationMBD[T]
}