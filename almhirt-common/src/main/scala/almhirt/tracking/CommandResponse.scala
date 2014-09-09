package almhirt.tracking

import almhirt.common.{ CommandId, Problem }

sealed trait CommandResponse
final case class CommandAccepted(id: CommandId) extends CommandResponse
final case class CommandNotAccepted(id: CommandId, reason: String) extends CommandResponse

sealed trait TrackedCommandResponse extends CommandResponse
final case class TrackedCommandResult(id: CommandId, status: CommandStatus) extends TrackedCommandResponse
final case class TrackedCommandTimedOut(id: CommandId) extends TrackedCommandResponse
final case class TrackerFailed(id: CommandId, problem: Problem) extends TrackedCommandResponse
