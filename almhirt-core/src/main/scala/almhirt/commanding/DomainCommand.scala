package almhirt.commanding

import org.joda.time.DateTime
import almhirt.common._
import almhirt.domain.AggregateRootRef

trait DomainCommandHeader extends CommandHeader {
  def aggRef: AggregateRootRef
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommandHeader
}

object DomainCommandHeader {
  def apply(anId: java.util.UUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: DateTime, metaData: Map[String, String]): DomainCommandHeader = BasicDomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, metaData)
  def apply(anId: java.util.UUID, anAggregateRoorRef: AggregateRootRef, aTimestamp: DateTime): CommandHeader = DomainCommandHeader(anId, anAggregateRoorRef, aTimestamp, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getDateTime, Map.empty)
  def apply(anAggregateRoorRef: AggregateRootRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = DomainCommandHeader(ccuad.getUuid, anAggregateRoorRef, ccuad.getDateTime, metaData)

  private case class BasicDomainCommandHeader(id: java.util.UUID, aggRef: AggregateRootRef, timestamp: DateTime, metadata: Map[String, String]) extends DomainCommandHeader {
    override def changeMetadata(newMetadata: Map[String, String]): BasicDomainCommandHeader =
      this.copy(metadata = newMetadata)
  }
}

trait DomainCommand extends Command { 
  override def header: DomainCommandHeader
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommand
  def creates: Boolean = header.aggRef.version == 0L
  def targettedVersion: Long = header.aggRef.version
  def targettedAggregateRoot: java.util.UUID = header.aggRef.id
}

object DomainCommand {
  implicit class DomainCommandOps[T <: DomainCommand](self: T){
    def isValidNonFirstGroupElement: AlmValidation[Boolean] =
      self.getGrouping.map{ grp => grp.index > 1 && self.targettedVersion == -1L }
    def isNonFirstGroupElement: Boolean =
      isValidNonFirstGroupElement.fold(fail => false, succ => succ)
    def isValidFirstGroupElement: AlmValidation[Boolean] =
      self.getGrouping.map{ grp => grp.index == 1 && (if(self.creates) self.targettedVersion == 0L else self.targettedVersion > 0L) }
    def isFirstGroupElement: Boolean =
      isValidFirstGroupElement.fold(fail => false, succ => succ)
    
  }
}
