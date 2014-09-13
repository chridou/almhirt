package almhirt.common

import scalaz.syntax.validation._

final case class EventId(value: String) extends AnyVal

object ValidatedEventId {
  def apply(value: String): AlmValidation[EventId] =
    CanCreateUuidsAndDateTimes.validateUniqueStringId(value).fold(
      fail => BadDataProblem(s""""$value" is not a valid event id.""").failure,
      succ => EventId(value).success)
}