package almhirt.aggregates

/** The version of an aggregate root. Used for optimistic concurrency and always starts with 0 whereas 0 
 *  means that the aggregate root is in state [[Vacat]]
 */
final case class AggregateRootVersion(val value: Long) extends AnyVal with Ordered[AggregateRootVersion]{
	def compare(that: AggregateRootVersion) =  this.value.compareTo(that.value) 
	def inc() = AggregateRootVersion(value + 1L)
}