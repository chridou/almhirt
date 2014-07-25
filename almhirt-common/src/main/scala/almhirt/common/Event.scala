package almhirt.common

import org.joda.time.LocalDateTime
import almhirt.common._

trait EventHeader {
  /** The events unique identifier */
  def id: EventId
  /** The events creation timestamp */
  def timestamp: LocalDateTime
  def metadata: Map[String, String]
  def changeMetadata(newMetaData: Map[String, String]): EventHeader
}

case class GenericEventHeader(id: EventId, timestamp: LocalDateTime, metadata: Map[String, String]) extends EventHeader {
  override def changeMetadata(newMetadata: Map[String, String]): GenericEventHeader =
    this.copy(metadata = newMetadata)
}

object EventHeader {
  def apply(anId: EventId, aTimestamp: LocalDateTime, metaData: Map[String, String]): EventHeader = GenericEventHeader(anId, aTimestamp, metaData)
  def apply(anId: EventId, aTimestamp: LocalDateTime): EventHeader = GenericEventHeader(anId, aTimestamp, Map.empty)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader = GenericEventHeader(EventId(ccuad.getUniqueString), ccuad.getUtcTimestamp, Map.empty)
  def apply(metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader = GenericEventHeader(EventId(ccuad.getUniqueString), ccuad.getUtcTimestamp, metaData)
}

trait Event {
  def header: EventHeader
  def metadata: Map[String, String] = header.metadata
  def changeMetadata(newMetaData: Map[String, String]): Event
  def eventId = header.id
  def timestamp = header.timestamp
}

object Event {
  implicit class EventOps[T <: Event](self: T) {
    def mergeMetadata(toMerge: Map[String, String]): T =
      self.changeMetadata(toMerge.foldLeft(self.metadata)((acc, cur) => acc + cur)).asInstanceOf[T]
    def addMetadata(newValue: (String, String)): T =
      self.changeMetadata(self.metadata + newValue).asInstanceOf[T]
  }
}
