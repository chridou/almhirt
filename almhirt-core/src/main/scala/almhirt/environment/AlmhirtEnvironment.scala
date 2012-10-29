package almhirt.environment

import almhirt._
import almhirt.commanding.CommandEnvelope
import almhirt.messaging._
import almhirt.parts._
import almhirt.eventlog.DomainEventLog

trait AlmhirtEnvironmentOps extends AlmhirtContextOps {
  
}

trait AlmhirtEnvironment extends AlmhirtEnvironmentOps with Disposable {
  def context: AlmhirtContext
  
  def reportProblem(prob: Problem) { context.reportProblem(prob) }
  def reportOperationState(opState: OperationState) { context.reportOperationState(opState) }

  def commandExecutor: CommandExecutor
  def repositories: HasRepositories
  def eventLog: DomainEventLog
  def operationStateTracker: util.OperationStateTracker
  
}