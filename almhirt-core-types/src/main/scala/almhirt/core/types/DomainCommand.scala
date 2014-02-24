package almhirt.core.types

import java.util.{ UUID => JUUID }
import scalaz._, Scalaz._
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

object DomainCommandSequence {
  def isPotentialCommandSequence(cmds: Seq[DomainCommand]): Boolean = cmds.exists(_.isPartOfAGroup)

  def validatedCandidates(cmds: Seq[DomainCommand]): AlmValidation[Seq[DomainCommand]] =
    if (cmds.isEmpty) {
      ConstraintViolatedProblem("The sequence may not be empty").failure
    } else {
      val headCmd = cmds.head
      if (!cmds.forall(_.targettedAggregateRootId == headCmd.targettedAggregateRootId)) {
        ConstraintViolatedProblem("All commands must target the same aggregate root.").failure
      } else if (!cmds.forall(_.targettedVersion == headCmd.targettedVersion)) {
        ConstraintViolatedProblem("All commands must target the same version.").failure
      } else if (isPotentialCommandSequence(cmds)) {
        ConstraintViolatedProblem("One or more commands are already part of a group.").failure
      } else {
        cmds.success
      }
    }

  def validatedCommandSequence(cmds: Seq[DomainCommand]): AlmValidation[Seq[DomainCommand]] =
    if (cmds.isEmpty) {
      ConstraintViolatedProblem("The sequence may not be empty").failure
    } else {
      val headCmd = cmds.head
      if (!cmds.forall(_.targettedAggregateRootId == headCmd.targettedAggregateRootId)) {
        ConstraintViolatedProblem("All commands must target the same aggregate root.").failure
      } else if (!cmds.forall(_.targettedVersion == headCmd.targettedVersion)) {
        ConstraintViolatedProblem("All commands must target the same version.").failure
      } else if (cmds.forall(_.isPartOfAGroup)) {
        ConstraintViolatedProblem("One or more commands are not part of a group.").failure
      } else if (cmds.forall(headCmd.tryGetGroupLabel == _.tryGetGroupLabel)) {
        ConstraintViolatedProblem("Group labels differ.").failure
      } else {
        val groupings = cmds.map(_.getGrouping.toOption).flatten
        val sortedIds = groupings.map(_.index).sorted
        if (groupings.map(_.index).toSet.size != groupings.size) {
          ConstraintViolatedProblem("There are duplicate indexes.").failure
        } else if (sortedIds.head != 1) {
          ConstraintViolatedProblem("The lowest index must be 1.").failure
        } else if (sortedIds.sliding(2).forall(x => x.last - x.head == 1)) {
          ConstraintViolatedProblem("There is at least one gap between the indexes.").failure
        } else {
          cmds.success
        }
      }
    }

  def makeCommandSequence(groupLabel: String, cmds: Seq[DomainCommand], trackable: Boolean): AlmValidation[Seq[DomainCommand]] =
    validatedCandidates(cmds).map(candidates => CommandGrouping.groupCommands(groupLabel, cmds.toList, trackable))

  def makeCommandSequence(cmds: Seq[DomainCommand], trackable: Boolean)(implicit ccuad: CanCreateUuid): AlmValidation[Seq[DomainCommand]] = 
    makeCommandSequence(ccuad.getUniqueString, cmds, trackable)
}
