package almhirt.environment.configuration.bootstrappers

import scalaz.syntax.validation._
import akka.actor.ActorSystem
import akka.event.Logging
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.environment.LogBackLoggingAdapter
import almhirt.messaging.MessageHub

trait CreatesAlmhirtFromConfigAndActorSystem extends CreatesAlmhirtBootstrapperPhase { self: HasConfig with HasServiceRegistry with HasActorSystem =>
  override def createAlmhirt(startUpLogger: LoggingAdapter): Either[BootstrapperPhaseFailure, (Almhirt, List[CleanUpAction])] =
    createTheAlmhirt(startUpLogger)

  private def createExecutionContext(actorSystem: ActorSystem, startUpLogger: LoggingAdapter, configPath: String): AlmValidation[HasExecutionContext] = {
    inTryCatch {
      val dispatcherName =
        ConfigHelper.getDispatcherNameFromComponentConfigPath(config)(configPath).fold(
          fail => {
            startUpLogger.warning("No dispatchername found. Using default Dispatcher")
            None
          },
          succ => {
            startUpLogger.info(s"ExecutionContext is using dispatcher '$succ'")
            Some(succ)
          })
      val dispatcher = ConfigHelper.lookUpDispatcher(actorSystem)(dispatcherName)
      HasExecutionContext(dispatcher)
    }
  }

  private def createFuturesExecutionContext(actorSystem: ActorSystem, startUpLogger: LoggingAdapter): AlmValidation[HasExecutionContext] = {
    startUpLogger.info("Creating FuturesExecutionContext ...")
    createExecutionContext(actorSystem, startUpLogger, ConfigPaths.futures)
  }

  private def createCruncherExecutionContext(actorSystem: ActorSystem, startUpLogger: LoggingAdapter): AlmValidation[HasExecutionContext] = {
    startUpLogger.info("Creating Cruncher ...")
    createExecutionContext(actorSystem, startUpLogger, ConfigPaths.cruncher)
  }
  
  private def createTheAlmhirt(startUpLogger: LoggingAdapter): Either[BootstrapperPhaseFailure, (Almhirt, List[CleanUpAction])] = {
    val theDurations = Durations(config)
    startUpLogger.info(s"Durations have been set to: ${theDurations.toString()}")
    startUpLogger.info("Creating Almhirt...")
    (for {
      almhirtLogger <- if (ConfigHelper.isBooleanSet(self.config)("almhirt.useAkkaLogging")) {
        startUpLogger.info("Using Akka system logger")
        Logging(self.actorSystem, classOf[Almhirt]).success
      } else {
        startUpLogger.info("Using logback logger")
        LogBackLoggingAdapter(ConfigHelper.getString(config)("almhirt.systemname").getOrElse("almhirt")).success
      }
      hasExecutionContext <- createFuturesExecutionContext(self.actorSystem, startUpLogger)
      theCruncher <- createCruncherExecutionContext(self.actorSystem, startUpLogger)
      theMessageHub <- MessageHub("MessageHub", config)(self.actorSystem, hasExecutionContext).success
    } yield {
      val theAlmhirt = new Almhirt with PostsOnMessageHub {
        override val actorSystem = self.actorSystem
        override val executionContext = hasExecutionContext.executionContext
        override val cruncher = theCruncher
        override val messageHub = theMessageHub
        override def getServiceByType(clazz: Class[_ <: AnyRef]) = self.serviceRegistry.getServiceByType(clazz)
        override val durations = theDurations
        override val log = almhirtLogger
      }
      val cleanUp = CleanUpAction(() => (), "Almhirt")
      (theAlmhirt, cleanUp)
    }).fold(
      fail => Left(BootstrapperPhaseFailure(fail, List(() => ()))),
      succ => Right(succ._1, List(succ._2)))
  }

}