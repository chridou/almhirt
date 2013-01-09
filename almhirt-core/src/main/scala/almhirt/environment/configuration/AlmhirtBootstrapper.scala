package almhirt.environment.configuration

import almhirt.common._
import almhirt.environment._
import com.typesafe.config.Config
import almhirt.core._

trait AlmhirtBootstrapper {
  def createAlmhirtSystem(): AlmValidation[(AlmhirtSystem, CleanUpAction)]

  def createServiceRegistry(theSystem: AlmhirtSystem): (Option[ServiceRegistry], CleanUpAction)

  def createChannels(theServiceRegistry: Option[ServiceRegistry])(implicit theSystem: AlmhirtSystem): AlmValidation[CleanUpAction]

  def createAlmhirt(theServiceRegistry: Option[ServiceRegistry])(implicit theSystem: AlmhirtSystem): AlmValidation[(Almhirt, CleanUpAction)]

  def createCoreComponents(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction]

  def initializeCoreComponents(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction]
  
  def registerRepositories(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction]

  def registerCommandHandlers(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction]
  
  def registerAndInitializeMoreComponents(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction]

  def prepareGateways(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction]
  
  def registerAndInitializeAuxServices(implicit theAlmhirt: Almhirt): AlmValidation[CleanUpAction]

  def cleanUpTemps(implicit theAlmhirt: Almhirt): AlmValidation[Unit]
}

object AlmhirtBootstrapper {
  def createFromConfig(config: Config): AlmValidation[AlmhirtBootstrapper] =
    SystemHelper.createBootstrapperFromConfig(config)

  def runStartupSequence(bootstrapper: AlmhirtBootstrapper): AlmValidation[(Almhirt, ShutDown)] =
    for {
      (system, cleanUp1) <- bootstrapper.createAlmhirtSystem()
      (serviceRegistry, cleanUp2) <- scalaz.Success(bootstrapper.createServiceRegistry(system))
      cleanUp3 <- bootstrapper.createChannels(serviceRegistry)(system)
      (almhirt, cleanUp4) <- bootstrapper.createAlmhirt(serviceRegistry)(system)
      cleanUp5 <- bootstrapper.createCoreComponents(almhirt)
      cleanUp6 <- bootstrapper.initializeCoreComponents(almhirt)
      cleanUp7 <- bootstrapper.registerRepositories(almhirt)
      cleanUp8 <- bootstrapper.registerCommandHandlers(almhirt)
      cleanUp9 <- bootstrapper.registerAndInitializeMoreComponents(almhirt)
      cleanUp10 <- bootstrapper.prepareGateways(almhirt)
      cleanUp11 <- bootstrapper.registerAndInitializeAuxServices(almhirt)
      _ <- bootstrapper.cleanUpTemps(almhirt)
    } yield (almhirt, new ShutDown{ def shutDown() { cleanUp11(); cleanUp10(); cleanUp9(); cleanUp8(); cleanUp7(); cleanUp6(); cleanUp5(); cleanUp4(); cleanUp3(); cleanUp2(); cleanUp1(); system.actorSystem.awaitTermination } })
}