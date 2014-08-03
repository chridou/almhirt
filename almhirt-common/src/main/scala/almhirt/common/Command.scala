package almhirt.common

import org.joda.time.LocalDateTime
import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }

trait Command {
  /** The commands unique identifier */
  def id: CommandId
  /** The events timestamp of creation */
  def timestamp: LocalDateTime
}

trait DomainCommand extends Command
trait AggregateCommand extends DomainCommand {
  def aggId: AggregateRootId
  def aggVersion: AggregateRootVersion
}

trait SystemCommand extends Command





