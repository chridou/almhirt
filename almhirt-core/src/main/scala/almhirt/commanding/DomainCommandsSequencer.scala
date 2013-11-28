package almhirt.commanding

import org.joda.time.DateTime
import akka.actor._
import almhirt.common._
import almhirt.core.types._

object DomainCommandsSequencer {
  sealed trait DomainCommandsSequencerMessage
  final case class SequenceDomainCommand(command: DomainCommand) extends DomainCommandsSequencerMessage
  final case class DomainCommandsSequenceCreated(groupLabel: String, sequence: Iterable[DomainCommand]) extends DomainCommandsSequencerMessage
  final case class DomainCommandsSequenceNotCreated(groupLabel: String, problem: Problem) extends DomainCommandsSequencerMessage
}

trait DomainCommandsSequencer { actor: Actor with ActorLogging =>
  def receiveDomainCommandsSequencerMessage: Receive
}

