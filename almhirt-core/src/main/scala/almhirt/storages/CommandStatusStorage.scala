package almhirt.storages

import org.joda.time.LocalDateTime
import almhirt.common._
import almhirt.tracking.CommandStatus

object CommandStatusStorage {
  final case class CommandStatusDocument(commandId: CommandId, timestamp: LocalDateTime, status: CommandStatus)
  
  sealed trait CommandStatusStorageMessage

  final case class StoreCommandStatus(status: CommandStatusDocument) extends CommandStatusStorageMessage
  sealed trait StoreCommandStatusResult extends CommandStatusStorageMessage
  final case class CommandStatusStored(commandId: CommandId) extends StoreCommandStatusResult
  final case class CommandStatusNotStored(commandId: CommandId, problem: Problem) extends StoreCommandStatusResult

  final case class FetchCommandStatus(commandId: CommandId) extends CommandStatusStorageMessage
  sealed trait FetchCommandStatusResult extends CommandStatusStorageMessage
  final case class CommandStatusFetched(status: CommandStatusDocument) extends FetchCommandStatusResult
  final case class CommandStatusNotFetched(commandId: CommandId, problem: Problem) extends FetchCommandStatusResult
}