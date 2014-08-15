package almhirt.aggregates

final case class AggregateRootVersion(val value: Long) extends AnyVal with Ordered[AggregateRootVersion]{
	def compare(that: AggregateRootVersion) =  this.value.compareTo(that.value) 
	def inc() = AggregateRootVersion(value + 1L)
}