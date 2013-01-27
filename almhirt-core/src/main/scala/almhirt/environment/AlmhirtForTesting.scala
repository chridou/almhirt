package almhirt.environment

import almhirt.core.ServiceRegistry
import almhirt.parts.HasRepositories
import almhirt.parts.HasCommandHandlers
import almhirt.eventlog.DomainEventLog
import almhirt.util._
import almhirt.commanding._
import almhirt.core.Almhirt
import almhirt.common.AlmValidation
import almhirt.core.PublishesOnMessageHub

trait AlmhirtForTesting extends Almhirt {
  def serviceRegistry: ServiceRegistry
  def repositories: HasRepositories
  def hasCommandHandlers: HasCommandHandlers
  def eventLog: DomainEventLog
  def operationStateTracker: OperationStateTracker
  def executeCommand(cmd: DomainCommand, ticket: Option[TrackingTicket]) { executeCommand(CommandEnvelope(cmd, ticket)) }
  def executeTrackedCommand(cmd: DomainCommand, ticket: TrackingTicket) { executeCommand(CommandEnvelope(cmd, Some(ticket))) }
  def executeUntrackedCommand(cmd: DomainCommand) { executeCommand(CommandEnvelope(cmd, None)) }
  def executeCommand(cmdEnv: CommandEnvelope): Unit
}

object AlmhirtForTesting {
  def apply(theAlmhirt: Almhirt, aServiceRegistry: ServiceRegistry): AlmValidation[AlmhirtForTesting] =
    for {
      theRepositories <- theAlmhirt.getService[HasRepositories]
      theCommandHandlers <- theAlmhirt.getService[HasCommandHandlers]
      anEventLog <- theAlmhirt.getService[DomainEventLog]
      theOperationStateTracker <- theAlmhirt.getService[OperationStateTracker]      
    } yield 
      new AlmhirtForTesting with PublishesOnMessageHub {
          override val actorSystem = theAlmhirt.actorSystem
          override val executionContext = theAlmhirt.executionContext
          override def getServiceByType(clazz: Class[_ <: AnyRef]) = theAlmhirt.getServiceByType(clazz)
          override val messageHub = theAlmhirt.messageHub

          override val serviceRegistry = aServiceRegistry
          override def executeCommand(cmdEnv: CommandEnvelope) { theAlmhirt.publishCommandEnvelope(cmdEnv) }

          override val durations = theAlmhirt.durations

          override def repositories = theRepositories
          override def hasCommandHandlers = theCommandHandlers
          override def eventLog = anEventLog
          override def operationStateTracker = theOperationStateTracker

          override def log = theAlmhirt.log
        }}