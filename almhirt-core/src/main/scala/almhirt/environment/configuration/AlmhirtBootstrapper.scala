//package almhirt.environment.configuration
//
//import scalaz.syntax.validation._
//import akka.event.LoggingAdapter
//import almhirt.common._
//import almhirt.environment._
//import almhirt.core._
//import com.typesafe.config.Config
//import akka.actor.ActorSystem
//import almhirt.core.Almhirt
//
//abstract class AlmhirtBootstrapper {
//  def createActorSystem(startUpLogger: LoggingAdapter): AlmValidation[(ActorSystem, CleanUpAction)] = 
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createActorSystem'").failure
//
//  def createServiceRegistry(theActorSystem: HasActorSystem, startUpLogger: LoggingAdapter): AlmValidation[(ServiceRegistry, CleanUpAction)] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createServiceRegistry'").failure
//
//  def createAlmhirt(hasActorSystem: HasActorSystem, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createAlmhirt'").failure
//
//  def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'createCoreComponents'").failure
//
//  def initializeCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'initializeCoreComponents'").failure
//
//  def registerRepositories(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerRepositories'").failure
//
//  def registerCommandHandlers(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerCommandHandlers'").failure
//
//  def registerAndInitializeMoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = 
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerAndInitializeMoreComponents'").failure
//
//  def prepareGateways(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'prepareGateways'").failure
//
//  def registerAndInitializeAuxServices(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = 
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'registerAndInitializeAuxServices'").failure
//
//  def cleanUpTemps(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[Unit] =
//    NotSupportedProblem("Your bootstrapper sequence is not complete. You must provide an implementation for 'cleanUpTemps'").failure
//}
//
//object AlmhirtBootstrapper {
//  def runStartupSequence(bootstrapper: AlmhirtBootstrapper, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, ShutDown)] =
//    for {
//      systemAndCleanUp1 <- 
//      	bootstrapper.createActorSystem(startUpLogger).map(x => 
//      	  (new HasActorSystem { val actorSystem = x._1 }, x._2)).bimap(prob => StartupProblem("Bootstrapper: 'createActorSystem' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      serviceRegistryAndCleanUp2 <- 
//      	bootstrapper.createServiceRegistry(systemAndCleanUp1._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'createServiceRegistry' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      almhirtAndCleanUp3 <- 
//      	bootstrapper.createAlmhirt(systemAndCleanUp1._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'createAlmhirt' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      cleanUp4 <- 
//      	bootstrapper.createCoreComponents(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'createCoreComponents' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      cleanUp5 <- 
//      	bootstrapper.initializeCoreComponents(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'initializeCoreComponents' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      cleanUp6 <- 
//      	bootstrapper.registerRepositories(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerRepositories' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      cleanUp7 <- 
//      	bootstrapper.registerCommandHandlers(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerCommandHandlers' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      cleanUp8 <- 
//      	bootstrapper.registerAndInitializeMoreComponents(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerAndInitializeMoreComponents' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      cleanUp9 <- 
//      	bootstrapper.prepareGateways(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'prepareGateways' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      cleanUp10 <- 
//      	bootstrapper.registerAndInitializeAuxServices(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'registerAndInitializeAuxServices' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//      _ <- 
//      	bootstrapper.cleanUpTemps(almhirtAndCleanUp3._1, serviceRegistryAndCleanUp2._1, startUpLogger).bimap(prob => StartupProblem("Bootstrapper: 'cleanUpTemps' failed.", cause=Some(CauseIsProblem(prob))), g => g)
//    } yield (almhirtAndCleanUp3._1, new ShutDown { def shutDown() { cleanUp10(); cleanUp9(); cleanUp8(); cleanUp7(); cleanUp6(); cleanUp5(); cleanUp4(); almhirtAndCleanUp3._2(); serviceRegistryAndCleanUp2._2(); systemAndCleanUp1._2() } })
//}