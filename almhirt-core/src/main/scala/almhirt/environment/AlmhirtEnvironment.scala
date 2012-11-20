package almhirt.environment

import almhirt.common._
import almhirt.core._
import almhirt.commanding.CommandEnvelope
import almhirt.messaging._
import almhirt.parts._
import almhirt.eventlog.DomainEventLog
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._
import com.typesafe.config.Config
import almhirt.environment.configuration.SystemHelper
import almhirt.common.AlmFuture

trait AlmhirtEnvironmentOps extends AlmhirtContextOps {
  def executeCommand(cmd: DomainCommand, ticket: Option[TrackingTicket]) { executeCommand(CommandEnvelope(cmd, ticket)) }
  def executeTrackedCommand(cmd: DomainCommand, ticket: TrackingTicket) { executeCommand(CommandEnvelope(cmd, Some(ticket))) }
  def executeUntrackedCommand(cmd: DomainCommand) { executeCommand(CommandEnvelope(cmd, None)) }
  def executeCommand(cmdEnv: CommandEnvelope): Unit
  def getReadOnlyRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmFuture[HasAggregateRoots[AR, TEvent]]
}

trait AlmhirtEnvironment extends AlmhirtEnvironmentOps with Disposable {
  def config: Config

  def context: AlmhirtContext

  def reportProblem(prob: Problem) { context.reportProblem(prob) }
  def reportOperationState(opState: OperationState) { context.reportOperationState(opState) }
  def executeCommand(cmdEnv: CommandEnvelope) { context.broadcastCommandEnvelope(cmdEnv) }
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { context.broadcast(payload, metaData) }
  def getDateTime = context.getDateTime
  def getUuid = context.getUuid
  def getReadOnlyRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmFuture[HasAggregateRoots[AR, TEvent]] =
    repositories.getForAggregateRoot
  def messageWithPayload[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = context.messageWithPayload(payload, metaData)

  def commandExecutor: CommandExecutor
  def repositories: HasRepositories
  def eventLog: DomainEventLog
  def operationStateTracker: almhirt.util.OperationStateTracker

  def addCommandHandler(handler: HandlesCommand) { commandExecutor.addHandler(handler) }
  def registerRepository[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]) { repositories.registerForAggregateRoot[AR, TEvent](repo) }
}

object AlmhirtEnvironment {
  import akka.pattern._
  import akka.util.Duration._
  import almhirt.syntax.almvalidation._
  import almhirt.almfuture.all._
  def apply(aConfig: Config)(implicit ctx: AlmhirtContext): AlmFuture[AlmhirtEnvironment] = {
    implicit val atMost = ctx.system.mediumDuration
    implicit val executor = ctx.system.futureDispatcher
    for {
      tracker <- AlmPromise(OperationStateTracker())
      trackerRegistration <- (ctx.operationStateChannel.actor ? SubscribeQry(MessagingSubscription.forActor[OperationState](tracker.actor)))(atMost)
        .mapTo[SubscriptionRsp]
        .map(_.registration)
        .toAlmFuture
      repos <- AlmPromise(HasRepositories())
      cmdExecutor <- AlmPromise(CommandExecutor(repos))
      cmdExecutorRegistration <- (ctx.commandChannel.actor ? SubscribeQry(MessagingSubscription.forActor[CommandEnvelope](cmdExecutor.actor)))(atMost)
        .mapTo[SubscriptionRsp]
        .map(_.registration)
        .toAlmFuture
      theEventLog <- AlmPromise(SystemHelper.createEventLogFromFactory(ctx))
    } yield (
      new AlmhirtEnvironment {
        val config = aConfig
        val context = ctx
        val repositories = repos
        val commandExecutor = cmdExecutor
        val eventLog = theEventLog
        val operationStateTracker = tracker
        def dispose {
          cmdExecutorRegistration.dispose
          trackerRegistration.dispose
          tracker.dispose
          context.dispose
        }
      })
  }
  def apply()(implicit ctx: AlmhirtContext): AlmFuture[AlmhirtEnvironment] = apply(ctx.config)

}