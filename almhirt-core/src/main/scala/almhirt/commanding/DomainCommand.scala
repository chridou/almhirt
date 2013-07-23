package almhirt.commanding

import java.util.{UUID => JUUID}
import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.domain.AggregateRootRef

trait DomainCommandHeader extends CommandHeader {
  def aggRef: AggregateRootRef
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommandHeader
}

object DomainCommandHeader {
  def apply(anId: JUUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: LocalDateTime, metaData: Map[String, String]): DomainCommandHeader = BasicDomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, metaData)
  def apply(anId: JUUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: LocalDateTime): DomainCommandHeader = DomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef)(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getUtcTimestamp, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getUtcTimestamp, metaData)

  private case class BasicDomainCommandHeader(id: JUUID, aggRef: AggregateRootRef, timestamp: LocalDateTime, metadata: Map[String, String]) extends DomainCommandHeader {
    override def changeMetadata(newMetadata: Map[String, String]): BasicDomainCommandHeader =
      this.copy(metadata = newMetadata)
  }
}


trait DomainCommand extends Command {
  override def header: DomainCommandHeader
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommand
  def creates: Boolean = this.isInstanceOf[CreatingDomainCommand]
  def targettedAggregateRootRef: AggregateRootRef = header.aggRef
  def targettedVersion: Long = header.aggRef.version
  def targettedAggregateRootId: java.util.UUID = header.aggRef.id
}

trait CreatingDomainCommand { self: DomainCommand => }
