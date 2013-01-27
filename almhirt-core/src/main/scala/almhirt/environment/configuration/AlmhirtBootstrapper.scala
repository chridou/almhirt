package almhirt.environment.configuration

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.environment._
import almhirt.core._
import com.typesafe.config.Config
import akka.actor.ActorSystem

trait AlmhirtBootstrapper {
  def createActorSystem(startUpLogger: LoggingAdapter): AlmValidation[(ActorSystem)]

  def createServiceRegistry(theActorSystem: ActorSystem, startUpLogger: LoggingAdapter): (ServiceRegistry, CleanUpAction)

  def createFuturesExecutionContext(theActorSystem: ActorSystem, startUpLogger: LoggingAdapter): AlmValidation[(HasExecutionContext, CleanUpAction)]
  
  def initializeMessaging(foundations: MessagingFoundations, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def createAlmhirt(theActorSystem: ActorSystem, hasFuturesExecutionContext: HasExecutionContext, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)]

  def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def initializeCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def registerRepositories(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def registerCommandHandlers(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def registerAndInitializeMoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def prepareGateways(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def registerAndInitializeAuxServices(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def cleanUpTemps(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[Unit]
}

object AlmhirtBootstrapper {
  def createFromConfig(config: Config): AlmValidation[AlmhirtBootstrapper] =
    SystemHelper.createBootstrapperFromConfig(config)

  def runStartupSequence(bootstrapper: AlmhirtBootstrapper, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, ShutDown)] =
    for {
      
      (system) <- bootstrapper.createActorSystem(startUpLogger)
      (serviceRegistry, cleanUp1) <- scalaz.Success(bootstrapper.createServiceRegistry(system, startUpLogger))
      (executionContext, cleanUp2) <- bootstrapper.createFuturesExecutionContext(system, startUpLogger)
      messagingFoundations <- MessagingFoundations(system, executionContext.executionContext).success
      cleanUp3 <- bootstrapper.initializeMessaging(messagingFoundations, serviceRegistry, startUpLogger)
      (almhirt, cleanUp4) <- bootstrapper.createAlmhirt(system, messagingFoundations, serviceRegistry, startUpLogger)
      cleanUp5 <- bootstrapper.createCoreComponents(almhirt, serviceRegistry, startUpLogger)
      cleanUp6 <- bootstrapper.initializeCoreComponents(almhirt, serviceRegistry, startUpLogger)
      cleanUp7 <- bootstrapper.registerRepositories(almhirt, serviceRegistry, startUpLogger)
      cleanUp8 <- bootstrapper.registerCommandHandlers(almhirt, serviceRegistry, startUpLogger)
      cleanUp9 <- bootstrapper.registerAndInitializeMoreComponents(almhirt, serviceRegistry, startUpLogger)
      cleanUp10 <- bootstrapper.prepareGateways(almhirt, serviceRegistry, startUpLogger)
      cleanUp11 <- bootstrapper.registerAndInitializeAuxServices(almhirt, serviceRegistry, startUpLogger)
      _ <- bootstrapper.cleanUpTemps(almhirt, serviceRegistry, startUpLogger)
    } yield (almhirt, new ShutDown { def shutDown() { cleanUp11(); cleanUp10(); cleanUp9(); cleanUp8(); cleanUp7(); cleanUp6(); cleanUp5(); cleanUp4(); cleanUp3(); cleanUp2(); cleanUp1() } })
}