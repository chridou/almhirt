package almhirt.common

import org.joda.time.LocalDateTime
import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }

case class CommandHeader(id: CommandId, timestamp: LocalDateTime)

object CommandHeader {
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader =
    CommandHeader(CommandId(ccuad.getUniqueString), ccuad.getUtcTimestamp)
  def apply(id: CommandId)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader =
    CommandHeader(id, ccuad.getUtcTimestamp)
}

trait Command {
  def header: CommandHeader
  final def commandId: CommandId = header.id
  final def timestamp: LocalDateTime = header.timestamp
}

trait DomainCommand extends Command
trait AggregateCommand extends DomainCommand {
  def aggId: AggregateRootId
  def aggVersion: AggregateRootVersion
}

trait SystemCommand extends Command





