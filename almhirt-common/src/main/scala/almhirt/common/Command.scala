package almhirt.common

import scalaz._, Scalaz._
import _root_.java.time.LocalDateTime
import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }

case class CommandHeader(id: CommandId, timestamp: LocalDateTime, metadata: Map[String, String])

object CommandHeader {
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader =
    CommandHeader(CommandId(ccuad.getUniqueString), ccuad.getUtcTimestamp, Map.empty)
  def apply(id: CommandId)(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader =
    CommandHeader(id, ccuad.getUtcTimestamp, Map.empty)
  def apply(id: CommandId, timestamp: LocalDateTime): CommandHeader =
    CommandHeader(id, timestamp, Map.empty)

  implicit class CommandHeaderOps(self: CommandHeader) {
    def withMetadata(metadata: Map[String, String]): CommandHeader =
      self.copy(metadata = metadata)
    def makeTrackable: CommandHeader =
      self.copy(metadata = self.metadata + ("trackable" → "true"))
      
    def isTrackable: Boolean = 
      self.metadata.get("trackable").map(_.toLowerCase() == "true") | false
  }
}

trait Command {
  def header: CommandHeader
  final def commandId: CommandId = header.id
  final def timestamp: LocalDateTime = header.timestamp
}

object Command {
 implicit class CommandOps(self: Command) {
    def isTrackable: Boolean = 
      self.header.isTrackable
  }
}

trait DomainCommand extends Command

trait AggregateRootCommand extends DomainCommand {
  def aggId: AggregateRootId
  def aggVersion: AggregateRootVersion
}

trait SystemCommand extends Command





