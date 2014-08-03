package almhirt.core.types

import scalaz._
Scalaz._
import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.aggregates.AggregateRootId
import almhirt.aggregates.AggregateRootVersion

trait DomainCommandHeader extends CommandHeader {
  def aggId: AggregateRootId
  def aggVersion: AggregateRootVersion
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommandHeader
}

case class GenericDomainCommandHeader(id: CommandId, aggId: AggregateRootId, aggVersion: AggregateRootVersion, timestamp: LocalDateTime, metadata: Map[String, String]) extends DomainCommandHeader {
  override def changeMetadata(newMetadata: Map[String, String]): GenericDomainCommandHeader =
    this.copy(metadata = newMetadata)
}

object DomainCommandHeader {
  def apply(anId: CommandId, aggIdAndVersion: (AggregateRootId, AggregateRootVersion), aTimestamp: LocalDateTime, metaData: Map[String, String]): DomainCommandHeader = GenericDomainCommandHeader(anId, aggIdAndVersion._1, aggIdAndVersion._2, aTimestamp, metaData)
  def apply(aggIdAndVersion: (AggregateRootId, AggregateRootVersion))(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = DomainCommandHeader(CommandId(ccuad.getUniqueString), aggIdAndVersion, ccuad.getUtcTimestamp, Map.empty)
  def apply(aggId: AggregateRootId, aggVersion: AggregateRootVersion)(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = DomainCommandHeader(aggId, aggVersion)
  def apply(aggIdAndVersion: (AggregateRootId, AggregateRootVersion), metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): DomainCommandHeader = DomainCommandHeader(CommandId(ccuad.getUniqueString), aggIdAndVersion, ccuad.getUtcTimestamp, metaData)

}

trait DomainCommand extends Command {
  override def header: DomainCommandHeader
  override def changeMetadata(newMetadata: Map[String, String]): DomainCommand
  def creates: Boolean = this.isInstanceOf[CreatingDomainCommand]
  def targettedAggregateRootRef: (AggregateRootId, AggregateRootVersion) = (header.aggId, header.aggVersion)
  def targettedVersion: AggregateRootVersion = header.aggVersion
  def targettedAggregateRootId: AggregateRootId = header.aggId
}

trait CreatingDomainCommand { self: DomainCommand => }

object DomainCommandSequence {
  def isPotentialCommandSequence(cmds: Seq[DomainCommand]): Boolean = cmds.exists(_.isPartOfAGroup)

  def validatedCandidates[T <: DomainCommand](cmds: Seq[T]): AlmValidation[Seq[T]] =
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

  def validatedCommandSequence[T <: DomainCommand](cmds: Seq[T]): AlmValidation[Seq[T]] =
    CommandGrouping.validatedCommandSequence(cmds).flatMap { cmds =>
      val headCmd = cmds.head
      if (!cmds.forall(_.targettedAggregateRootId == headCmd.targettedAggregateRootId)) {
        ConstraintViolatedProblem("All commands must target the same aggregate root.").failure
      } else if (!cmds.forall(_.targettedVersion == headCmd.targettedVersion)) {
        ConstraintViolatedProblem("All commands must target the same version.").failure
      } else {
        val groupings = cmds.map(_.getGrouping.toOption).flatten
        val sortedIds = groupings.map(_.index).sorted
        if (groupings.map(_.index).toSet.size != groupings.size) {
          ConstraintViolatedProblem("There are duplicate indexes.").failure
        } else if (sortedIds.head != 1) {
          ConstraintViolatedProblem("The lowest index must be 1.").failure
        } else if (sortedIds.sliding(2).exists(x => x.last - x.head != 1)) {
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
