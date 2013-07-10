package almhirt.common

import org.joda.time.DateTime
import almhirt.common._

trait CommandHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: DateTime
  /**
   *
   */
  def metaData: Map[String, String]

  def changeMetaData(newMetaData: Map[String, String]): CommandHeader
}

object CommandHeader {
  def apply(anId: java.util.UUID, aTimestamp: DateTime, metaData: Map[String, String]): CommandHeader = BasicCommandHeader(anId, aTimestamp, metaData)
  def apply(anId: java.util.UUID, aTimestamp: DateTime): CommandHeader = BasicCommandHeader(anId, aTimestamp, Map.empty)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = BasicCommandHeader(ccuad.getUuid, ccuad.getDateTime, Map.empty)
  def apply(metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = BasicCommandHeader(ccuad.getUuid, ccuad.getDateTime, metaData)
  private case class BasicCommandHeader(id: java.util.UUID, timestamp: DateTime, metaData: Map[String, String]) extends CommandHeader {
    override def changeMetaData(newMetaData: Map[String, String]): CommandHeader =
      this.copy(metaData = newMetaData)
  }
}


trait Command {
  def header: CommandHeader
  def changeMetaData(newMetaData: Map[String, String]): Command
}

trait CommandTemplate extends Command {
  protected def changeHeader(newHeader: CommandHeader): Command
  override final def changeMetaData(newMetaData: Map[String, String]): Command =
    changeHeader(this.header.changeMetaData(newMetaData))
}