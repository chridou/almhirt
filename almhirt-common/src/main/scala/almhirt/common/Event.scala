package almhirt.common

import org.joda.time.DateTime
import almhirt.common._

trait EventHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: DateTime
  /**
   * 
   */
  def sender: Option[String]
}

final case class BasicEventHeader(id: java.util.UUID, timestamp: DateTime, sender: Option[String]) extends EventHeader

object EventHeader {
  def apply(anId: java.util.UUID, aTimestamp: DateTime, sender: Option[String]): EventHeader = BasicEventHeader(anId, aTimestamp, sender)
  def apply(anId: java.util.UUID, aTimestamp: DateTime): EventHeader = BasicEventHeader(anId, aTimestamp, None)
  def apply(anId: java.util.UUID, aTimestamp: DateTime, sender: String): EventHeader = BasicEventHeader(anId, aTimestamp, Some(sender))
}

trait Event {
  def header: EventHeader
}

