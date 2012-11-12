package almhirt.domain

import almhirt.core._
import almhirt.common._

case class AggregateRootHasBeenDeletedProblem(message: String = "The aggregate root has been deleted.", id: java.util.UUID, severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = AggregateRootHasBeenDeletedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}
