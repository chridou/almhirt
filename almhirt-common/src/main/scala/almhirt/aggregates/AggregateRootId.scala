package almhirt.aggregates

import scalaz.syntax.validation._
import almhirt.common._

/**
 * The id of an aggregate root. Used to identify an aggregate root independently from its current
 *  lifecycle state.
 */
final case class AggregateRootId(value: String) extends AnyVal

object ValidatedAggregatedRootId {
  def apply(value: String): AlmValidation[AggregateRootId] =
    CanCreateUuidsAndDateTimes.validateUniqueStringId(value).fold(
      fail ⇒ BadDataProblem(s""""$value" is not a valid aggregate root id.""").failure,
      succ ⇒ AggregateRootId(value).success)
}