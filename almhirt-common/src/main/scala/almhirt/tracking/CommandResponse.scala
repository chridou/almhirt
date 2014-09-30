package almhirt.tracking

import almhirt.common.{ CommandId, Problem }

sealed trait CommandResponse
final case class CommandAccepted(id: CommandId) extends CommandResponse
final case class CommandNotAccepted(id: CommandId, why: RejectionReason) extends CommandResponse

sealed trait TrackedCommandResponse extends CommandResponse
final case class TrackedCommandResult(id: CommandId, status: CommandStatus.CommandResult) extends TrackedCommandResponse
final case class TrackedCommandTimedOut(id: CommandId) extends TrackedCommandResponse
final case class TrackerFailed(id: CommandId, problem: Problem) extends TrackedCommandResponse

sealed trait RejectionReason
object RejectionReason {
  final case class TooBusy(msg: String) extends RejectionReason
  final case class NotReady(msg: String) extends RejectionReason
  final case class AProblem(problem: Problem) extends RejectionReason
}
