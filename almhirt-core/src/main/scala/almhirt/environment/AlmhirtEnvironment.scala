package almhirt.environment

import almhirt._
import almhirt.commanding.CommandEnvelope
import almhirt.messaging._
import almhirt.parts._
import almhirt.eventlog.DomainEventLog
import almhirt.commanding._
import almhirt.domain._

trait AlmhirtEnvironmentOps extends AlmhirtContextOps {
  def executeCommand(cmd: DomainCommand, ticket: Option[String])
  def executeCommandWithTicket(cmd: DomainCommand, ticket: String) { executeCommand(cmd, Some(ticket)) }
  def executeCommandWithOutTicket(cmd: DomainCommand) { executeCommand(cmd, None) }
}

trait AlmhirtEnvironment extends AlmhirtEnvironmentOps with Disposable {
  def context: AlmhirtContext

  def reportProblem(prob: Problem) { context.reportProblem(prob) }
  def reportOperationState(opState: OperationState) { context.reportOperationState(opState) }
  def executeCommand(cmdEnv: CommandEnvelope) { context.executeCommand(cmdEnv) }
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String]) { context.broadcast(payload, metaData) }
  def getDateTime = context.getDateTime
  def getUuid = context.getUuid

  def commandExecutor: CommandExecutor
  def repositories: HasRepositories
  def eventLog: DomainEventLog
  def operationStateTracker: util.OperationStateTracker

  def addCommandHandler(handler: HandlesCommand) { commandExecutor.addHandler(handler) }
  def registerRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, T <: AggregateRootRepository[_, _]](repo: T)(implicit m: Manifest[AR]) { repositories.registerForAggregateRoot(repo) }

  def executeCommand(cmd: DomainCommand, ticket: Option[String]) { context.executeCommand(CommandEnvelope(cmd, ticket)) }

}