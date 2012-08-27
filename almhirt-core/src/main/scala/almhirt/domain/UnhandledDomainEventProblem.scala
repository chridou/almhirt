package almhirt.domain

import almhirt.validation.{Severity, Major, SystemProblem, Problem}

/** Used to indicate that an event couldn't be handled by an aggregate root's handler. 
 * This is a major problem as it indicates a software defect.
 */
case class UnhandledDomainEventProblem(message: String, unhandledEvent: DomainEvent, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends SystemProblem {
  type T = UnhandledDomainEventProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withException(err: Throwable) = copy(exception = Some(err))
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}
