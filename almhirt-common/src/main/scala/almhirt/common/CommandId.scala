package almhirt.common

import scalaz.syntax.validation._

final case class CommandId(value: String) extends AnyVal

object ValidatedCommandId {
  def apply(value: String): AlmValidation[CommandId] =
    CanCreateUuidsAndDateTimes.validateUniqueStringId(value).fold(
      fail ⇒ BadDataProblem(s""""$value" is not a valid command id.""").failure,
      succ ⇒ CommandId(value).success)
}