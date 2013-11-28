package almhirt.core.types

import java.util.{UUID => JUUID}
import org.joda.time.LocalDateTime
import almhirt.common._

trait DomainCommandHeader extends CommandHeader {
  def aggRef: AggregateRootRef
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommandHeader
}

object DomainCommandHeader {
  def apply(anId: JUUID, anAggregateRootRef: AggregateRootRef, aTimestamp: LocalDateTime, metaData: Map[String, String]): DomainCommandHeader = BasicDomainCommandHeader(anId, anAggregateRootRef, aTimestamp, metaData)
  def apply(anId: JUUID, anAggregateRootRef: AggregateRootRef, aTimestamp: LocalDateTime): DomainCommandHeader = DomainCommandHeader(anId, anAggregateRootRef, aTimestamp, Map.empty)
  def apply(anAggregateRootRef: AggregateRootRef)(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRootRef, ccuad.getUtcTimestamp, Map.empty)
  def apply(aggIdAndVersion: (JUUID, Long))(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = 
    DomainCommandHeader(AggregateRootRef(aggIdAndVersion._1, aggIdAndVersion._2))
  def apply(anAggregateRootRef: AggregateRootRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRootRef, ccuad.getUtcTimestamp, metaData)

  case class BasicDomainCommandHeader(id: JUUID, aggRef: AggregateRootRef, timestamp: LocalDateTime, metadata: Map[String, String]) extends DomainCommandHeader {
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
