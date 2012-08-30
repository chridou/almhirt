package almhirt.validation

import scalaz.Validation
import scalaz.syntax.validation._

sealed trait AlmMatcher[T]

case class AlmSuccess[T](succ: T) extends AlmMatcher[T]
case class AlmFailure[T](p: Problem) extends AlmMatcher[T]

//object AlmSuccess {
//  def unapply[R](validation: AlmValidation[R]): Option[R] = 
//    validation fold (f => None, succ => Some(succ))
//}

//object AlmFailure {
//  def unapply[P <: Problem, R](validation: Validation[P,R]): Option[P] = 
//    validation fold (f => Some(f), succ => None)
//}

object AlmSeverity {
  def unapply[R](validation: AlmValidation[R]): Option[Severity] = 
    validation fold (f => Some(f.severity), succ => None)
}

object AlmCategory {
  def unapply[R](validation: AlmValidation[R]): Option[ProblemCategory] = 
    validation fold (f => Some(f.category), succ => None)
}