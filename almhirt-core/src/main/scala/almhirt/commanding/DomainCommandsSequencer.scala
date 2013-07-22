package almhirt.commanding

import org.joda.time.DateTime
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.domain.AggregateRootRef

object DomainCommandsSequencer {
  sealed trait DomainCommandsSequencerMessage
  final case class SequenceDomainCommand(command: DomainCommand) extends DomainCommandsSequencerMessage
  final case class DomainCommandsSequenceCreated(groupLabel: String, sequence: Iterable[DomainCommand]) extends DomainCommandsSequencerMessage
  final case class DomainCommandsSequenceNotCreated(groupLabel: String, problem: Problem) extends DomainCommandsSequencerMessage
}

trait DomainCommandsSequencer { actor: Actor with ActorLogging =>
  def receiveDomainCommandsSequencerMessage: Receive
}

trait DomainCommandsSequencerTemplate extends DomainCommandsSequencer { actor: Actor with ActorLogging =>
  import DomainCommandsSequencer._

  private case class SequenceEntry(groupLabel: String, aggRef: AggregateRootRef, expectedSequenceSize: Option[Int], collectedCommands: Map[Int, DomainCommand], birthDate: DateTime, responsible: ActorRef) {
    def addCommand(command: DomainCommand): AlmValidation[SequenceEntry] =
      command.getGrouping.flatMap { grp =>
        if (command.targettedAggregateRootRef != aggRef)
          CollisionProblem(s"""All commands in the sequence "${this.groupLabel}" must target the same aggregate root "${aggRef.id}" with the same version("${aggRef.version}"). The command to be added targets aggregate root "${command.targettedAggregateRootId}" with version ${command.targettedVersion}.""").failure
        else if (collectedCommands.contains(grp.index))
          CollisionProblem(s"""There is already a command with index ${grp.index} in the sequence "${this.groupLabel}". The command with id "${command.commandId}" of type "${command.getClass().getName()}" cannot be added to the sequence.""").failure
        else if (grp.isLast && expectedSequenceSize.isEmpty)
          SequenceEntry(this.groupLabel, this.aggRef, Some(grp.index), this.collectedCommands + (grp.index -> command), this.birthDate, this.responsible).success
        else if (grp.isLast && expectedSequenceSize.isDefined)
          UnspecifiedProblem(s"""The size of the sequence "${this.groupLabel}" was already determined. Since the current command with id "${command.commandId}" of type "${command.getClass().getName()}" is marked as the last in the sequence, did you set "isLast" on more than one command?""").failure
        else //(!grp.isLast))
          SequenceEntry(this.groupLabel, this.aggRef, this.expectedSequenceSize, this.collectedCommands + (grp.index -> command), this.birthDate, this.responsible).success
      }

    def isComplete: Boolean =
      expectedSequenceSize match {
        case Some(seqSize) => collectedCommands.size == seqSize
        case None => false
      }

    def getOnlyValid: AlmValidation[SequenceEntry] = {
      val idxSet = collectedCommands.keySet
      if (idxSet.min != 1)
        UnspecifiedProblem(s"""The sequence "${this.groupLabel}" does not start with an index of 1.""").failure
      else if (idxSet.max != idxSet.size)
        UnspecifiedProblem(s"""The sequence "${this.groupLabel}" does not contain consecutive indexes.""").failure
      else
        this.success
    }

    def commandSequence: Iterable[DomainCommand] = collectedCommands.toList.sortBy(x => x._1).map(_._2)
  }

  implicit def theAlmhirt: Almhirt

  def receiveDomainCommandsSequencerMessage = transitionToNextState(Map.empty)

  def transitionToNextState(currentState: Map[String, SequenceEntry]): Receive = {
    case SequenceDomainCommand(command) =>
      val updatedSequences = processIncomingCommand(command, sender, currentState)
      val completeSequencesProcessed = processCompletedSequences(updatedSequences)
      this.context.become(transitionToNextState(completeSequencesProcessed))
  }

  private def processCompletedSequences(currentState: Map[String, SequenceEntry]): Map[String, SequenceEntry] =
    currentState.foldLeft(currentState) { (acc, cur) =>
      val (label, entry) = cur
      if (entry.isComplete) {
        entry.getOnlyValid.fold(
          fail => entry.responsible ! DomainCommandsSequenceNotCreated(label, fail),
          succ => succ.responsible ! DomainCommandsSequenceCreated(label, succ.commandSequence))
        acc - label
      } else {
        acc
      }
    }

  private def processIncomingCommand(command: DomainCommand, responsible: ActorRef, currentState: Map[String, SequenceEntry]): Map[String, SequenceEntry] =
    command.tryGetGroupLabel match {
      case Some(groupLabel) =>
        log.debug(s"""Sequence command "${command.commandId}" for "$groupLabel".""")
        currentState.get(groupLabel) match {
          case Some(entry) =>
            addToEntry(command, entry).fold(
              fail => {
                entry.responsible ! DomainCommandsSequenceNotCreated(groupLabel, fail)
                currentState - groupLabel
              },
              modifiedEntry => currentState + (groupLabel -> modifiedEntry))
          case None =>
            createNewEntry(command, sender).fold(
              fail => {
                sender ! DomainCommandsSequenceNotCreated(groupLabel, fail)
                currentState
              },
              newEntry => currentState + (groupLabel -> newEntry))
        }
      case None =>
        log.warning(s"""Command "${command.getClass().getName()}" with id "${command.commandId}" of type "${command.getClass().getName()}" targetting aggregate root "${command.targettedAggregateRootId}" with version "${command.targettedVersion}" is not a member of a group""")
        currentState
    }

  private def createNewEntry(command: DomainCommand, responsible: ActorRef): AlmValidation[SequenceEntry] = {
    command.getGrouping.map(grp =>
      if (grp.isLast)
        SequenceEntry(grp.groupLabel, command.targettedAggregateRootRef, Some(grp.index), Map(grp.index -> command), theAlmhirt.getDateTime, responsible)
      else
        SequenceEntry(grp.groupLabel, command.targettedAggregateRootRef, None, Map(grp.index -> command), theAlmhirt.getDateTime, responsible))
  }

  private def addToEntry(command: DomainCommand, entry: SequenceEntry): AlmValidation[SequenceEntry] =
    entry.addCommand(command)

}