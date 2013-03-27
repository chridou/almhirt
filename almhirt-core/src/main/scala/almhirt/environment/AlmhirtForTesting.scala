package almhirt.environment

import almhirt.common._
import almhirt.commanding._
import almhirt.core._
import almhirt.parts._
import almhirt.eventlog.DomainEventLog
import almhirt.util._

trait AlmhirtForTesting extends Almhirt {
  def getRepositories: AlmValidation[HasRepositories]
  def getHasCommandHandlers: AlmValidation[HasCommandHandlers]
  def getEventLog: AlmValidation[DomainEventLog]
  def getOperationStateTracker: AlmValidation[OperationStateTracker]
  def executeCommand(cmd: DomainCommand, ticket: Option[TrackingTicket]) { executeCommand(CommandEnvelope(cmd, ticket)) }
  def executeTrackedCommand(cmd: DomainCommand, ticket: TrackingTicket) { executeCommand(CommandEnvelope(cmd, Some(ticket))) }
  def executeUntrackedCommand(cmd: DomainCommand) { executeCommand(CommandEnvelope(cmd, None)) }
  def executeCommand(cmdEnv: CommandEnvelope): Unit
}

trait AlmhirtForExtendedTesting extends AlmhirtForTesting {
  def serviceRegistry: ServiceRegistry
  def repositories: HasRepositories
  def hasCommandHandlers: HasCommandHandlers
  def domainEventLog: DomainEventLog
  def operationStateTracker: OperationStateTracker
}

object AlmhirtForTesting {
  def apply(theAlmhirt: Almhirt): AlmhirtForTesting =
    new AlmhirtForTesting with PublishesOnMessageHub {
      override val actorSystem = theAlmhirt.actorSystem
      override val executionContext = theAlmhirt.executionContext
      override def getServiceByType(clazz: Class[_ <: AnyRef]) = theAlmhirt.getServiceByType(clazz)
      override val messageHub = theAlmhirt.messageHub

      override def executeCommand(cmdEnv: CommandEnvelope) { theAlmhirt.publishCommandEnvelope(cmdEnv) }

      override val durations = theAlmhirt.durations

      override def getRepositories = theAlmhirt.getService[HasRepositories]
      override def getHasCommandHandlers = theAlmhirt.getService[HasCommandHandlers]
      override def getEventLog = theAlmhirt.getService[DomainEventLog]
      override def getOperationStateTracker = theAlmhirt.getService[OperationStateTracker]

      override def log = theAlmhirt.log
    }
}

object AlmhirtForExtendedTesting {
  def apply(theAlmhirt: Almhirt, aServiceRegistry: ServiceRegistry): AlmValidation[AlmhirtForExtendedTesting] =
    for {
      theRepositories <- theAlmhirt.getService[HasRepositories]
      theCommandHandlers <- theAlmhirt.getService[HasCommandHandlers]
      anEventLog <- theAlmhirt.getService[DomainEventLog]
      theOperationStateTracker <- theAlmhirt.getService[OperationStateTracker]
    } yield new AlmhirtForExtendedTesting with PublishesOnMessageHub {
      override val actorSystem = theAlmhirt.actorSystem
      override val executionContext = theAlmhirt.executionContext
      override def getServiceByType(clazz: Class[_ <: AnyRef]) = theAlmhirt.getServiceByType(clazz)
      override val messageHub = theAlmhirt.messageHub

      override val serviceRegistry = aServiceRegistry
      override def executeCommand(cmdEnv: CommandEnvelope) { theAlmhirt.publishCommandEnvelope(cmdEnv) }

      override val durations = theAlmhirt.durations

      override def getRepositories = theAlmhirt.getService[HasRepositories]
      override def getHasCommandHandlers = theAlmhirt.getService[HasCommandHandlers]
      override def getEventLog = theAlmhirt.getService[DomainEventLog]
      override def getOperationStateTracker = theAlmhirt.getService[OperationStateTracker]

      override val repositories = theRepositories
      override val hasCommandHandlers = theCommandHandlers
      override val domainEventLog = anEventLog
      override val operationStateTracker = theOperationStateTracker
      
      override def log = theAlmhirt.log
    }
}