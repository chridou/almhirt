package almhirt.commanding

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import akka.actor._
import almhirt.common._
import almhirt.commanding._

object DomainCommandsSequencer {
  sealed trait DomainCommandsSequencerMessage
  final case class SequenceDomainCommand(command: DomainCommand) extends DomainCommandsSequencerMessage
  final case class DomainCommandsSequenceCreated(sequence: Iterable[DomainCommand]) extends DomainCommandsSequencerMessage
  final case class DomainCommandsSequenceNotCreated(groupLabel: String, problem: Problem) extends DomainCommandsSequencerMessage
}

trait DomainCommandsSequencer { actor: Actor with ActorLogging =>
  def receiveDomainCommandsSequencerMessage: Receive
}

trait DomainCommandsSequencerTemplate extends DomainCommandsSequencer { actor: Actor with ActorLogging =>
  import DomainCommandsSequencer._

  private case class SequenceEntry(groupLabel: String, arId: JUUID, sequenceSize: Option[Int], collectedCommands: Map[Int, DomainCommand], age: DateTime, responsible: ActorRef)

  def transitionToNextState(currentState: Map[String, SequenceEntry]): Receive = {
    case SequenceDomainCommand(command) =>
      val nextState =
        command.tryGetGroupLabel match {
          case Some(groupLabel) =>
            currentState.get(groupLabel) match {
              case Some(entry) =>
                addToEntry(command, entry).fold(
                  fail => {
                    entry.responsible ! DomainCommandsSequenceNotCreated(groupLabel, fail)
                    currentState
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
            log.warning(s"""Command "${command.getClass().getName()}" with id "${command.header}" targetting aggregate root "${command.targettedAggregateRoot}" with version "${command.targettedVersion}" is not a member of a group""")
            currentState
        }
      this.context.become(transitionToNextState(nextState))
  }

  private def createNewEntry(command: DomainCommand, responsible: ActorRef): AlmValidation[SequenceEntry] = {
    //    for {
    //      
    //    }
    //command.getGrouping(commandGrouping)
    ???
  }

  private def addToEntry(command: DomainCommand, entry: SequenceEntry): AlmValidation[SequenceEntry] =
    ???

}