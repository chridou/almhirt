package almhirt.environment.configuration

import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.environment._
import almhirt.core._
import com.typesafe.config.Config
import akka.actor.ActorSystem
import almhirt.core.Almhirt

abstract class AlmhirtBootstrapper {
  def createActorSystem(startUpLogger: LoggingAdapter): AlmValidation[(ActorSystem, CleanUpAction)] = 
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createActorSystem'").failure

  def createServiceRegistry(theActorSystem: HasActorSystem, startUpLogger: LoggingAdapter): AlmValidation[(ServiceRegistry, CleanUpAction)] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createServiceRegistry'").failure

  def createAlmhirt(hasActorSystem: HasActorSystem, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createAlmhirt'").failure

  def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createCoreComponents'").failure

  def initializeCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'initializeCoreComponents'").failure

  def registerRepositories(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerRepositories'").failure

  def registerCommandHandlers(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerCommandHandlers'").failure

  def registerAndInitializeMoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = 
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerAndInitializeMoreComponents'").failure

  def prepareGateways(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'prepareGateways'").failure

  def registerAndInitializeAuxServices(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = 
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerAndInitializeAuxServices'").failure

  def cleanUpTemps(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[Unit] =
    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'cleanUpTemps'").failure
}

object AlmhirtBootstrapper {
  def runStartupSequence(bootstrapper: AlmhirtBootstrapper, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, ShutDown)] =
    for {
      (system, cleanUp1) <- 
      	bootstrapper.createActorSystem(startUpLogger).map(x => 
      	  (new HasActorSystem { val actorSystem = x._1 }, x._2)).bimap(prob => StartupProblem("Bootstrapper: 'createActorSystem' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      (serviceRegistry, cleanUp2) <- 
      	bootstrapper.createServiceRegistry(system, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'createServiceRegistry' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      (almhirt, cleanUp3) <- 
      	bootstrapper.createAlmhirt(system, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'createAlmhirt' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      cleanUp4 <- 
      	bootstrapper.createCoreComponents(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'createCoreComponents' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      cleanUp5 <- 
      	bootstrapper.initializeCoreComponents(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'initializeCoreComponents' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      cleanUp6 <- 
      	bootstrapper.registerRepositories(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerRepositories' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      cleanUp7 <- 
      	bootstrapper.registerCommandHandlers(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerCommandHandlers' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      cleanUp8 <- 
      	bootstrapper.registerAndInitializeMoreComponents(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerAndInitializeMoreComponents' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      cleanUp9 <- 
      	bootstrapper.prepareGateways(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'prepareGateways' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      cleanUp10 <- 
      	bootstrapper.registerAndInitializeAuxServices(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerAndInitializeAuxServices' failed.", cause=Some(CauseIsProblem(prob))), g => g)
      _ <- 
      	bootstrapper.cleanUpTemps(almhirt, serviceRegistry, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'cleanUpTemps' failed.", cause=Some(CauseIsProblem(prob))), g => g)
    } yield (almhirt, new ShutDown { def shutDown() { cleanUp10(); cleanUp9(); cleanUp8(); cleanUp7(); cleanUp6(); cleanUp5(); cleanUp4(); cleanUp3(); cleanUp2(); cleanUp1() } })
}