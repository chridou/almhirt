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
  override def createActorSystem(startUpLogger: LoggingAdapter): AlmValidation[(ActorSystem, CleanUpAction)] = {
    val sysName = ConfigHelper.getString(config)("almhirt.systemname").getOrElse("almhirt")
    startUpLogger.info(s"Creating ActorSystem with name $sysName ...")
    val system = ActorSystem(sysName, config)
    (system, () => { system.shutdown(); system.awaitTermination() }).success
  }

  override def createServiceRegistry(system: HasActorSystem, startUpLogger: LoggingAdapter): AlmValidation[(ServiceRegistry, CleanUpAction)] = {
    startUpLogger.info("Creating ServiceRegistry...")
    val registry = new SimpleConcurrentServiceRegistry()
    registry.registerService[HasConfig](new HasConfig { override val config = AlmhirtBaseBootstrapper.this.config })
    (registry, () => ()).success
  }

  private def createFuturesExecutionContext(actorSystem: ActorSystem, startUpLogger: LoggingAdapter): AlmValidation[HasExecutionContext] = {
    startUpLogger.info("Creating FuturesExecutionContext...")
    val dispatcherName =
      ConfigHelper.getDispatcherNameFromComponentConfigPath(config)(ConfigPaths.futures).fold(
        fail => {
          startUpLogger.warning("No dispatchername found for FuturesExecutionContext. Using default Dispatcher")
          None
        },
        succ => {
          startUpLogger.info(s"FuturesExecutionContext is using dispatcher '$succ'")
          Some(succ)
        })
    val dispatcher = ConfigHelper.lookUpDispatcher(actorSystem)(dispatcherName)
    HasExecutionContext(dispatcher).success
  }

  override def createAlmhirt(hasActorSystem: HasActorSystem, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)] = {
    val theDurations = Durations(config)
    startUpLogger.info(s"Durations have been set to: ${theDurations.toString()}")

    startUpLogger.info("Creating Almhirt...")
    for {
      hasExecutionContext <- createFuturesExecutionContext(hasActorSystem.actorSystem, startUpLogger)
      theMessageHub <- MessageHub("MessageHub", config)(hasActorSystem, hasExecutionContext).success
    } yield (new Almhirt with PublishesOnMessageHub {
      override val actorSystem = hasActorSystem.actorSystem
      override val executionContext = hasExecutionContext.executionContext
      override val messageHub = theMessageHub
      override def getServiceByType(clazz: Class[_ <: AnyRef]) = theServiceRegistry.getServiceByType(clazz)
      override val durations = theDurations
      override val log = Logging(actorSystem, classOf[Almhirt])
    }, () => ())
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