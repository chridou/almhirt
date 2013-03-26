//package almhirt.environment.configuration.impl
//
//import akka.event.LoggingAdapter
//import almhirt.environment.configuration.AlmhirtBootstrapper
//import almhirt.core.HasActorSystem
//import almhirt.core.ServiceRegistry
//import almhirt.environment.configuration.CleanUpAction
//import almhirt.core.impl.SimpleConcurrentServiceRegistry
//import almhirt.core.ServiceRegistry
//import almhirt.common.AlmValidation
//
//trait RegistersServiceRegistry extends AlmhirtBootstrapper { 
//  override def createServiceRegistry(system: HasActorSystem, startUpLogger: LoggingAdapter): AlmValidation[(ServiceRegistry, CleanUpAction)] = {
//    super.createServiceRegistry(system, startUpLogger).map {
//      case (registry, cleanUp) =>
//        startUpLogger.info("Register the ServiceRegistry to itself")
//        registry.registerService[ServiceRegistry](registry)
//        (registry, cleanUp)
//    }
//  }
//}