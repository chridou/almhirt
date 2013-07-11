package almhirt.domain

import almhirt.common._
import almhirt.problem._

case object AggregateRootDeletedProblem extends ProblemType {
  def apply(id: java.util.UUID, msg: String = "The aggregate root has been deleted.", args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None): SingleProblem = {
    val completeArgs = args + ("ar_id" -> id)
    SingleProblem(msg, UnspecifiedProblem, completeArgs, cause)
  }
  def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, AggregateRootDeletedProblem)
}
