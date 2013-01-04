package riftwarp

import almhirt.common._

/**
 * Some data couldn't be serialized
 */
case class RiftWarpProblem(message: String, severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = RiftWarpProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}
