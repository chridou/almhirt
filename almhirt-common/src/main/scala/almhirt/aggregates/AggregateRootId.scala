package almhirt.aggregates

/** The id of an aggregate root. Used to identify an aggregate root independently from its current
 *  lifecycle state.
 */
final case class AggregateRootId(value: String) extends AnyVal