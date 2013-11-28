package almhirt.core.types

import almhirt.common._
import almhirt.problem._

case object AggregateRootDeletedProblem extends ProblemType {
  def apply(id: java.util.UUID, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None): SingleProblem = {
    val completeArgs = args + ("aggregate-root-id" -> id)
    SingleProblem(s"""The aggregate root with id "${id.toString()}" has been deleted.""", UnspecifiedProblem, completeArgs, cause)
  }
  def unapply(problem: SingleProblem): Option[SingleProblem] = SingleProblem.unapplyAgainst(problem, AggregateRootDeletedProblem)
}
