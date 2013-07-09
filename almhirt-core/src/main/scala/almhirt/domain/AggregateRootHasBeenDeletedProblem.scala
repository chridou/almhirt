package almhirt.domain

import almhirt.common._
import almhirt.problem._

case object AggregateRootHasBeenDeletedProblem extends ProblemType {
  def apply(msg: String = "The aggregate root has been deleted.", id: java.util.UUID, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None): SingleProblem = {
    val completeArgs = args + ("ar_id" -> id)
    SingleProblem(msg, UnspecifiedProblem, completeArgs, cause)
  }
  def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, AggregateRootHasBeenDeletedProblem)
}
