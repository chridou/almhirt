package almhirt.environment.configuration

import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.environment._
import almhirt.core._
import com.typesafe.config.Config

trait AlmhirtBootstrapper {
  def createAlmhirtSystem(startUpLogger: LoggingAdapter): AlmValidation[(AlmhirtSystem, CleanUpAction)]

  def createServiceRegistry(theSystem: AlmhirtSystem, startUpLogger: LoggingAdapter): (Option[ServiceRegistry], CleanUpAction)

  def createChannels(theServiceRegistry: Option[ServiceRegistry], startUpLogger: LoggingAdapter)(implicit theSystem: AlmhirtSystem): AlmValidation[CleanUpAction]

  def createAlmhirt(theServiceRegistry: Option[ServiceRegistry], startUpLogger: LoggingAdapter)(implicit theSystem: AlmhirtSystem): AlmValidation[(Almhirt, CleanUpAction)]

  def createCoreComponents(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def initializeCoreComponents(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]
  
  def registerRepositories(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def registerCommandHandlers(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]
  
  def registerAndInitializeMoreComponents(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def prepareGateways(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]
  
  def registerAndInitializeAuxServices(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction]

  def cleanUpTemps(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[Unit]
}

object AlmhirtBootstrapper {
  def createFromConfig(config: Config): AlmValidation[AlmhirtBootstrapper] =
    SystemHelper.createBootstrapperFromConfig(config)

  def runStartupSequence(bootstrapper: AlmhirtBootstrapper, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, ShutDown)] =
    for {
      (system, cleanUp1) <- bootstrapper.createAlmhirtSystem(startUpLogger)
      (serviceRegistry, cleanUp2) <- scalaz.Success(bootstrapper.createServiceRegistry(system, startUpLogger))
      cleanUp3 <- bootstrapper.createChannels(serviceRegistry, startUpLogger)(system)
      (almhirt, cleanUp4) <- bootstrapper.createAlmhirt(serviceRegistry, startUpLogger)(system)
      cleanUp5 <- bootstrapper.createCoreComponents(almhirt, startUpLogger)
      cleanUp6 <- bootstrapper.initializeCoreComponents(almhirt, startUpLogger)
      cleanUp7 <- bootstrapper.registerRepositories(almhirt, startUpLogger)
      cleanUp8 <- bootstrapper.registerCommandHandlers(almhirt, startUpLogger)
      cleanUp9 <- bootstrapper.registerAndInitializeMoreComponents(almhirt, startUpLogger)
      cleanUp10 <- bootstrapper.prepareGateways(almhirt, startUpLogger)
      cleanUp11 <- bootstrapper.registerAndInitializeAuxServices(almhirt, startUpLogger)
      _ <- bootstrapper.cleanUpTemps(almhirt, startUpLogger)
    } yield (almhirt, new ShutDown{ def shutDown() { cleanUp11(); cleanUp10(); cleanUp9(); cleanUp8(); cleanUp7(); cleanUp6(); cleanUp5(); cleanUp4(); cleanUp3(); cleanUp2(); cleanUp1(); system.actorSystem.awaitTermination } })
}