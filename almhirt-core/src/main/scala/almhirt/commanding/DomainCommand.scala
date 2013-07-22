package almhirt.commanding

import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.domain.AggregateRootRef

trait DomainCommandHeader extends CommandHeader {
  def aggRef: AggregateRootRef
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommandHeader
}

object DomainCommandHeader {
  def apply(anId: java.util.UUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: LocalDateTime, metaData: Map[String, String]): DomainCommandHeader = BasicDomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, metaData)
  def apply(anId: java.util.UUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: LocalDateTime): CommandHeader = DomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getUtcTimestamp, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getUtcTimestamp, metaData)

  private case class BasicDomainCommandHeader(id: java.util.UUID, aggRef: AggregateRootRef, timestamp: LocalDateTime, metadata: Map[String, String]) extends DomainCommandHeader {
    override def changeMetadata(newMetadata: Map[String, String]): BasicDomainCommandHeader =
      this.copy(metadata = newMetadata)
  }
}

trait DomainCommand extends Command {
  override def header: DomainCommandHeader
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommand
  def creates: Boolean = header.aggRef.version == 0L
  def targettedAggregateRootRef: AggregateRootRef = header.aggRef
  def targettedVersion: Long = header.aggRef.version
  def targettedAggregateRootId: java.util.UUID = header.aggRef.id
}

