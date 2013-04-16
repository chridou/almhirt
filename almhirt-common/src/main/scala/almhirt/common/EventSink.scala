package almhirt.common

trait EventSink {
  def consume(event: Event): Unit
}