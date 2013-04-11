package almhirt.eventlog

import almhirt.core.Event

trait EventSink {
  def consume(event: Event): Unit
}