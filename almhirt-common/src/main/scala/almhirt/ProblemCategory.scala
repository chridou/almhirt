package almhirt

sealed trait ProblemCategory{
  def and(other: ProblemCategory): ProblemCategory = 
    (this,other) match {
    case(SystemProblem,_) => SystemProblem
    case(_,SystemProblem) => SystemProblem
    case _  => ApplicationProblem
  }
}

case object SystemProblem extends ProblemCategory
case object ApplicationProblem extends ProblemCategory