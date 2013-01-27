package almhirt.environment.configuration

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.environment._
import almhirt.core._
import com.typesafe.config.Config
import akka.actor.ActorSystem
import almhirt.core.Almhirt

trait AlmhirtBootstrapper {
  def createActorSystem(startUpLogger: LoggingAdapter): AlmValidation[(ActorSystem)]

  def createServiceRegistry(theActorSystem: HasActorSystem, startUpLogger: LoggingAdapter): (ServiceRegistry, CleanUpAction)

  def createAlmhirt(hasActorSystem: HasActorSystem, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)]

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

      (system) <- bootstrapper.createActorSystem(startUpLogger).map(sys => new HasActorSystem {   val actorSystem = sys })
      (serviceRegistry, cleanUp1) <- scalaz.Success(bootstrapper.createServiceRegistry(system, startUpLogger))
      (almhirt, cleanUp2) <- bootstrapper.createAlmhirt(system, serviceRegistry, startUpLogger)
      cleanUp3 <- bootstrapper.createCoreComponents(almhirt, serviceRegistry, startUpLogger)
      cleanUp4 <- bootstrapper.initializeCoreComponents(almhirt, serviceRegistry, startUpLogger)
      cleanUp5 <- bootstrapper.registerRepositories(almhirt, serviceRegistry, startUpLogger)
      cleanUp6 <- bootstrapper.registerCommandHandlers(almhirt, serviceRegistry, startUpLogger)
      cleanUp7 <- bootstrapper.registerAndInitializeMoreComponents(almhirt, serviceRegistry, startUpLogger)
      cleanUp8 <- bootstrapper.prepareGateways(almhirt, serviceRegistry, startUpLogger)
      cleanUp9 <- bootstrapper.registerAndInitializeAuxServices(almhirt, serviceRegistry, startUpLogger)
      _ <- bootstrapper.cleanUpTemps(almhirt, serviceRegistry, startUpLogger)
    } yield (almhirt, new ShutDown { def shutDown() { cleanUp9(); cleanUp8(); cleanUp7(); cleanUp6(); cleanUp5(); cleanUp4(); cleanUp3(); cleanUp2(); cleanUp1() } })
}