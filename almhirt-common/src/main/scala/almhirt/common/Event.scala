package almhirt.common

import org.joda.time.DateTime
import almhirt.common._

trait EventHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: DateTime
  def metaData: Map[String, String]
  def changeMetaData(newMetaData: Map[String, String]): EventHeader
}

object EventHeader {
  def apply(anId: java.util.UUID, aTimestamp: DateTime, metaData: Map[String, String]): EventHeader = BasicEventHeader(anId, aTimestamp, metaData)
  def apply(anId: java.util.UUID, aTimestamp: DateTime): EventHeader = BasicEventHeader(anId, aTimestamp, Map.empty)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader = BasicEventHeader(ccuad.getUuid, ccuad.getDateTime, Map.empty)
  def apply(metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): EventHeader = BasicEventHeader(ccuad.getUuid, ccuad.getDateTime, metaData)
  private case class BasicEventHeader(id: java.util.UUID, timestamp: DateTime, metaData: Map[String, String]) extends EventHeader {
    override def changeMetaData(newMetaData: Map[String, String]): EventHeader =
      this.copy(metaData = newMetaData)
  }
}

trait Event {
  def header: EventHeader
  def changeMetaData(newMetaData: Map[String, String]): Event
}

trait EventTemplate[T <: Event] { self: Event =>
  protected def changeHeader(newHeader: EventHeader): T
  override final def changeMetaData(newMetaData: Map[String, String]): T =
    changeHeader(this.header.changeMetaData(newMetaData))
}

