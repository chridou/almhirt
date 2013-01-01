package almhirt.environment

import almhirt.parts.HasRepositories
import almhirt.parts.HasCommandHandlers
import almhirt.eventlog.DomainEventLog
import almhirt.util._
import almhirt.commanding._

trait AlmhirtForTesting extends Almhirt {
  def repositories: HasRepositories
  def hasCommandHandlers: HasCommandHandlers
  def eventLog: DomainEventLog
  def operationStateTracker: OperationStateTracker
  def executeCommand(cmd: DomainCommand, ticket: Option[TrackingTicket]) { executeCommand(CommandEnvelope(cmd, ticket)) }
  def executeTrackedCommand(cmd: DomainCommand, ticket: TrackingTicket) { executeCommand(CommandEnvelope(cmd, Some(ticket))) }
  def executeUntrackedCommand(cmd: DomainCommand) { executeCommand(CommandEnvelope(cmd, None)) }
  def executeCommand(cmdEnv: CommandEnvelope): Unit
}