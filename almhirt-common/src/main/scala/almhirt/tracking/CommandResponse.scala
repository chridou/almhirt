package almhirt.tracking

import almhirt.common._

sealed trait CommandResponse
final case class CommandAccepted(id: CommandId) extends CommandResponse
final case class CommandNotAccepted(id: CommandId, problem: Problem) extends CommandResponse

sealed trait TrackedCommandResponse extends CommandResponse
final case class TrackedCommandResult(id: CommandId, status: CommandResult) extends TrackedCommandResponse
final case class TrackingFailed(id: CommandId, problem: Problem) extends TrackedCommandResponse
