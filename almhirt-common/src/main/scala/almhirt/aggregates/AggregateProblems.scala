package almhirt.aggregates

import almhirt.common._
import almhirt.problem._

case object AggregateRootDeletedProblem extends ProblemType {
  def apply(id: AggregateRootId, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None): SingleProblem = {
    val completeArgs = args + ("aggregate-root-id" → id)
    SingleProblem(s"""The aggregate root with id "${id.value.toString()}" has been deleted.""", AggregateRootDeletedProblem, completeArgs, cause)
  }
  def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, AggregateRootDeletedProblem)
}
