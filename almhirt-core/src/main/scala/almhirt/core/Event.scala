package almhirt.core

import org.joda.time.DateTime

trait EventHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: DateTime  
}

case class BasicEventHeader(id: java.util.UUID, timestamp: DateTime) extends EventHeader

object EventHeader {
  def apply(anId: java.util.UUID, aTimestamp: DateTime): EventHeader = BasicEventHeader(anId, aTimestamp)
}

trait Event {
  def header: EventHeader
}