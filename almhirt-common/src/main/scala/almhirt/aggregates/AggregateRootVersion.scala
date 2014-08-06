package almhirt.aggregates

final case class AggregateRootVersion(val version: Long) extends AnyVal with Ordered[AggregateRootVersion]{
	def compare(that: AggregateRootVersion) =  this.version.compareTo(that.version) 
}