package almhirt.environment.configuration.impl

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.environment._
import almhirt.core.impl.SimpleConcurrentServiceRegistry
import almhirt.domain._
import almhirt.commanding._
import almhirt.util._
import com.typesafe.config.Config
import almhirt.environment.configuration.AlmhirtBootstrapper

class AlmhirtBaseBootstrapper(val config: Config) extends AlmhirtBootstrapper {
  private var serviceRegistry = new SimpleConcurrentServiceRegistry
  private var system: AlmhirtSystem = null
  private var context: AlmhirtContext = null
  private var almhirt: Almhirt = null

  def createAlmhirtSystem(): AlmValidation[AlmhirtSystem] =
    AlmhirtSystem(config)

  def createAlmhirtContext(system: AlmhirtSystem): AlmValidation[AlmhirtContext] = {
    this.system = system
    AlmhirtContext()(system).awaitResult(akka.util.Duration(5, "s"))
  }

  def wireChannels(context: AlmhirtContext): AlmValidation[AlmhirtContext] =
    context.success

  def createAlmhirt(context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Almhirt] = {
    this.context = context

    val almhirt =
      new Almhirt {
        def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: akka.util.Duration, m: Manifest[TPayload]) = context.messageHub.createMessageChannel(name)

        def executeCommand(cmdEnv: CommandEnvelope) { context.postCommand(cmdEnv) }

        def reportProblem(prob: Problem) { context.reportProblem(prob) }
        def reportOperationState(opState: OperationState) { context.reportOperationState(opState) }
        def broadcastDomainEvent(event: DomainEvent) { context.broadcastDomainEvent(event) }
        def postCommand(comEnvelope: CommandEnvelope) { context.postCommand(comEnvelope) }
        def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) { context.broadcast(payload, metaData) }
        def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = context.createMessage(payload, metaData)

        def registerServiceByType(clazz: Class[_ <: AnyRef], service: AnyRef) { serviceRegistry.registerServiceByType(clazz, service) }
        def getServiceByType(clazz: Class[_ <: AnyRef]) = serviceRegistry.getServiceByType(clazz)

        def futureDispatcher = system.futureDispatcher
        def shortDuration = system.shortDuration
        def mediumDuration = system.mediumDuration
        def longDuration = system.longDuration
        
        def getDateTime = system.getDateTime
        def getUuid = system.generateUuid

        def close() { AlmhirtBootstrapper.runShutDownSequence(AlmhirtBaseBootstrapper.this) }
      }

    this.almhirt = almhirt
    almhirt.success
  }

  def registerChannels(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] = {
    almhirt.registerService[CommandChannel](new CommandChannelWrapper(context.commandChannel))
    almhirt.registerService[DomainEventsChannel](new DomainEventsChannelWrapper(context.domainEventsChannel))
    almhirt.registerService[OperationStateChannel](new OperationStateChannelWrapper(context.operationStateChannel))
    almhirt.registerService[ProblemChannel](new ProblemChannelWrapper(context.problemChannel))
    ().success
  }

  
  def registerComponents(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] =
    ().success

  def registerServicesStage1(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] =
    ().success

  def registerRepositories(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] =
    ().success

  def registerCommandHandlers(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] =
    ().success

  def registerServicesStage2(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] =
    ().success

  def beforeClosing(): AlmValidation[Unit] = {
    ().success
  }
    
  def closing(): AlmValidation[Unit] = {
    context.dispose
    system.dispose
    ().success
  }

  def closed(): AlmValidation[Unit] = {
    ().success
  }

}