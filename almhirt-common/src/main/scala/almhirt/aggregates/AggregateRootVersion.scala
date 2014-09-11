package almhirt.aggregates

import scalaz._, Scalaz._
import almhirt.common._

/**
 * The version of an aggregate root. Used for optimistic concurrency and always starts with 0 whereas 0
 *  means that the aggregate root is in state [[Vacat]]
 */
final case class AggregateRootVersion(val value: Long) extends AnyVal with Ordered[AggregateRootVersion] {
  def compare(that: AggregateRootVersion) = this.value.compareTo(that.value)
  def inc() = AggregateRootVersion(value + 1L)
}

object ValidatedAggregateRootVersion {
  def apply(value: Long): AlmValidation[AggregateRootVersion] =
    if (value >= 0L)
      AggregateRootVersion(value).success
    else
      BadDataProblem(s"$value is not a valid version. it must be greater or equal than 0").failure
}