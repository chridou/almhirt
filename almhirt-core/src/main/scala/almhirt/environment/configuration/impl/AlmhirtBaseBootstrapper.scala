package almhirt.environment.configuration.impl

import scala.reflect.ClassTag
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.event._
import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.core._
import almhirt.environment._
import almhirt.core.impl.SimpleConcurrentServiceRegistry
import almhirt.domain._
import almhirt.commanding._
import almhirt.messaging._
import almhirt.util._
import almhirt.core.ServiceRegistry
import almhirt.environment.configuration._
import com.typesafe.config.Config
import almhirt.core.Almhirt
import almhirt.core.HasConfig

class AlmhirtBaseBootstrapper(override val config: Config) extends AlmhirtBootstrapper with HasConfig {
  override def createActorSystem(startUpLogger: LoggingAdapter): AlmValidation[ActorSystem] = {
    val sysName = ConfigHelper.getString(config)("almhirt.systemname").getOrElse("almhirt")
    ActorSystem(sysName, config).success
  }

  override def createServiceRegistry(system: HasActorSystem, startUpLogger: LoggingAdapter): (ServiceRegistry, CleanUpAction) = {
    val registry = new SimpleConcurrentServiceRegistry()
    registry.registerService[HasConfig](new HasConfig { override val config = AlmhirtBaseBootstrapper.this.config })
    (new SimpleConcurrentServiceRegistry, () => ())
  }

  private def createFuturesExecutionContext(actorSystem: ActorSystem, startUpLogger: LoggingAdapter): AlmValidation[HasExecutionContext] = {
    val dispatcherPath = ConfigHelper.lookupDispatcherConfigPath(config)(ConfigPaths.futures).toOption
    val dispatcher = ConfigHelper.lookUpDispatcher(actorSystem)(dispatcherPath)
    HasExecutionContext(dispatcher).success
  }

  override def createAlmhirt(hasActorSystem: HasActorSystem, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)] = {
    for {
      hasExecutionContext <- createFuturesExecutionContext(hasActorSystem.actorSystem, startUpLogger)
      theMessageHub <- MessageHub("MessageHub", config)(hasActorSystem, hasExecutionContext).success
    } yield (new Almhirt with PublishesOnMessageHub {
      override val actorSystem = hasActorSystem.actorSystem
      override val executionContext = hasExecutionContext.executionContext
      override val messageHub = theMessageHub
      override def getServiceByType(clazz: Class[_ <: AnyRef]) = theServiceRegistry.getServiceByType(clazz)
      override val durations = Durations(config)
      override val log = Logging(actorSystem, classOf[Almhirt])
    }, () => { hasActorSystem.actorSystem.shutdown(); hasActorSystem.actorSystem.awaitTermination() })
  }

  override def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = 
    (() => ()).success

  override def initializeCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerRepositories(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerCommandHandlers(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerAndInitializeMoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def prepareGateways(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def registerAndInitializeAuxServices(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    (() => ()).success

  override def cleanUpTemps(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[Unit] =
    ().success

}