package almhirt.core

import org.joda.time.DateTime

trait EventHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: DateTime  
}

trait Event {
  def header: EventHeader
}