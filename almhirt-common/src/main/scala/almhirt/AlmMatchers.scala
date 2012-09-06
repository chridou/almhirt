package almhirt

sealed trait AlmMatcher

case class AlmSuccess[T](succ: T) extends AlmMatcher
case class AlmFailure(p: Problem) extends AlmMatcher

object AlmSeverity {
  def unapply[R](validation: AlmValidation[R]): Option[Severity] = 
    validation fold (f => Some(f.severity), succ => None)
}

object AlmCategory {
  def unapply[R](validation: AlmValidation[R]): Option[ProblemCategory] = 
    validation fold (f => Some(f.category), succ => None)
}