package almhirt.common

trait EventConsumer extends Consumer[Event] {
  def consume(event: Event): Unit
}