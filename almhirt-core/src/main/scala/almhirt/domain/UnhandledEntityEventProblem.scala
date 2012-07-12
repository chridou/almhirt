package almhirt.domain

import almhirt.validation.{Severity, Major, SystemProblem, Problem}

case class UnhandledEntityEventProblem(message: String, unhandledEvent: EntityEvent, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), cause: Option[Problem] = None) extends SystemProblem {
  type T = UnhandledEntityEventProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withException(err: Throwable) = copy(exception = Some(err))
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}
