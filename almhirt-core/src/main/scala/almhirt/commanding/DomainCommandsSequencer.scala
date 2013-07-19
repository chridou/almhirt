package almhirt.commanding

import almhirt.common._

object DomainCommandsSequencer {
	sealed trait DomainCommandsSequencerMessage
	final case class SequenceDomainCommand(command: DomainCommand) extends DomainCommandsSequencerMessage
	final case class DomainCommandsSequenceCreated(sequence: Iterable[DomainCommand]) extends DomainCommandsSequencerMessage
	final case class DomainCommandsSequenceNotCreated(groupLabel: String, problem: Problem) extends DomainCommandsSequencerMessage
}