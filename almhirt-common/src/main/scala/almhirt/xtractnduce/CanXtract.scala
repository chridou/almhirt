package almhirt.xtractnduce

import scalaz.syntax.validation._
import almhirt._
import almhirt.syntax.almvalidation._

/** Use as a type class or mix into an object*/
trait CanXTract[T] {
  def xtract(xtractor: XTractor): AlmValidationMBD[T]
  def tryXtractFrom(xtractor: XTractor, key: String): AlmValidationMBD[Option[T]] =
    xtractor.tryGetXTractor(key).toMBD.bind(opt => opt.map(x => xtract(x)).validationOut)
  def xtractFrom(xtractor: XTractor, key: String): AlmValidationMBD[T] = 
    tryXtractFrom(xtractor, key) bind {
      case Some(x) => x.success 
      case None => SingleBadDataProblem("Key not found.", key = key).toMBD.failure } 
}