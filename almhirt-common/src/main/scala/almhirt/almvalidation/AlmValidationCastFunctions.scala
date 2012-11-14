package almhirt.almvalidation

import scalaz.syntax.validation._
import almhirt.common._

trait AlmValidationCastFunctions {
  def almCast[To](what: Any): AlmValidation[To] =
    try {
      what.asInstanceOf[To].success
    } catch {
      case exn => TypeCastProblem("Could not cast a type at runtime", cause = Some(CauseIsThrowable(exn))).failure
    }
}