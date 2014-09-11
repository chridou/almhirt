package almhirt.aggregates

import scalaz._, Scalaz._
import almhirt.common._

/** The id of an aggregate root. Used to identify an aggregate root independently from its current
 *  lifecycle state.
 */
final case class AggregateRootId(value: String) extends AnyVal

object ValidatedAggregatedRootId {
  private val regexStr = """(?:[-\w:@&=+,.!~*'_;]|%\p{XDigit}{2})(?:[-\w:@&=+,.!~*'$_;]|%\p{XDigit}{2})*"""
  private val regex = regexStr.r
  def apply(value: String): AlmValidation[AggregateRootId] = {
    if((regex findFirstIn value).nonEmpty) {
      AggregateRootId(value).success
    } else {
      BadDataProblem(s""""$value" is not a valid aggragate root id. It must conform to the regular expression "$regexStr".""").failure
    }
  }
}