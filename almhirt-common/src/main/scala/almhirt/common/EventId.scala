package almhirt.common

final case class EventId(id: String) extends AnyVal with Ordered[EventId]{
	def compare(that: EventId) =  this.id.compareTo(that.id) 
}