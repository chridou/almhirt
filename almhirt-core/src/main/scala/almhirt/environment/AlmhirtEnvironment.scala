package almhirt.environment

import almhirt._
import almhirt.commanding.CommandEnvelope
import almhirt.messaging._
import almhirt.parts._
import almhirt.eventlog.DomainEventLog

trait AlmhirtEnvironment extends Disposable {
  def context: AlmhirtContext
  
  def reportProblem(prob: Problem) { context.reportProblem(prob) }

  def commandExecutor: CommandExecutor
  def repositories: HasRepositories
  def eventLog: DomainEventLog
  
}