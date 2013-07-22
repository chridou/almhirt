package almhirt.common

import org.joda.time.LocalDateTime
import almhirt.common._

trait EventHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: LocalDateTime
  def metadata: Map[String, String]
  def changeMetadata(newMetaData: Map[String, String]): EventHeader
}

object EventHeader {
  def apply(anId: java.util.UUID, aTimestamp: LocalDateTime, metaData: Map[String, String]): EventHeader = BasicEventHeader(anId, aTimestamp, metaData)
  def apply(anId: java.util.UUID, aTimestamp: LocalDateTime): EventHeader = BasicEventHeader(anId, aTimestamp, Map.empty)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader = BasicEventHeader(ccuad.getUuid, ccuad.getUtcTimestamp, Map.empty)
  def apply(metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader = BasicEventHeader(ccuad.getUuid, ccuad.getUtcTimestamp, metaData)
  private case class BasicEventHeader(id: java.util.UUID, timestamp: LocalDateTime, metadata: Map[String, String]) extends EventHeader {
    override def changeMetadata(newMetadata: Map[String, String]): EventHeader =
      this.copy(metadata = newMetadata)
  }
}

trait Event {
  def header: EventHeader
  def metadata: Map[String, String] = header.metadata
  def changeMetadata(newMetaData: Map[String, String]): Event
}

object Event {
  implicit class EventOps[T <: Event](self: T) {
    def mergeMetadata(toMerge: Map[String, String]): T =
      self.changeMetadata(toMerge.foldLeft(self.metadata)((acc, cur) => acc + cur)).asInstanceOf[T]
    def addMetadata(newValue: (String, String)): T =
      self.changeMetadata(self.metadata + newValue).asInstanceOf[T]
  }
}
