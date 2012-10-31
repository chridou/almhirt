package almhirt.environment

import almhirt._
import almhirt.commanding.CommandEnvelope
import almhirt.messaging._
import almhirt.parts._
import almhirt.eventlog.DomainEventLog
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._

trait AlmhirtEnvironmentOps extends AlmhirtContextOps {
  def executeCommand(cmd: DomainCommand, ticket: Option[String]) { executeCommand(CommandEnvelope(cmd, ticket)) } 
  def executeTrackedCommand(cmd: DomainCommand, ticket: String) { executeCommand(CommandEnvelope(cmd, Some(ticket))) }
  def executeUntrackedCommand(cmd: DomainCommand) { executeCommand(CommandEnvelope(cmd, None)) }
  def executeCommand(cmdEnv: CommandEnvelope): Unit
  def getRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmValidation[AggregateRootRepository[AR, TEvent]]
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
  def registerRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, T <: AggregateRootRepository[AR, TEvent]](repo: T)(implicit m: Manifest[AR]) { repositories.registerForAggregateRoot[AR, TEvent, T](repo) }

  def getRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmValidation[AggregateRootRepository[AR, TEvent]] =
    repositories.getForAggregateRoot

}