package almhirt.commanding

import org.joda.time.DateTime
import almhirt.common._
import almhirt.domain.AggregateRootRef

trait DomainCommandHeader extends CommandHeader {
  def aggRef: AggregateRootRef
  override def changeMetaData(newMetaData: Map[String, String]): DomainCommandHeader
}

object DomainCommandHeader {
  def apply(anId: java.util.UUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: DateTime, metaData: Map[String, String]): DomainCommandHeader = BasicDomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, metaData)
  def apply(anId: java.util.UUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: DateTime): CommandHeader = DomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getDateTime, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getDateTime, metaData)

  private case class BasicDomainCommandHeader(id: java.util.UUID, aggRef: AggregateRootRef, timestamp: DateTime, metaData: Map[String, String]) extends DomainCommandHeader {
    override def changeMetaData(newMetaData: Map[String, String]): DomainCommandHeader =
      this.copy(metaData = newMetaData)
  }
}

trait DomainCommand extends Command { 
  override def header: DomainCommandHeader
  def creates: Boolean = header.aggRef.version == 0L
}

trait DomainCommandTemplate extends DomainCommand {
  protected def changeHeader(newHeader: DomainCommandHeader): DomainCommand
  override final def changeMetaData(newMetaData: Map[String, String]): DomainCommand =
    changeHeader(this.header.changeMetaData(newMetaData))
}