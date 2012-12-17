package almhirt.environment

import almhirt.parts.HasRepositories
import almhirt.parts.HasCommandHandlers
import almhirt.eventlog.DomainEventLog
import almhirt.util.OperationStateTracker

trait AlmhirtForTesting extends Almhirt {
  def system: AlmhirtSystem
  def context: AlmhirtContext
  def repositories: HasRepositories
  def hasCommandHandlers: HasCommandHandlers
  def eventLog: DomainEventLog
  def operationStateTracker: OperationStateTracker
}