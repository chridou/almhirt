package almhirt.environment.configuration.bootstrappers

import akka.event.LoggingAdapter
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.parts.HasCommandHandlers

trait CreatesAndRegistersHasCommandHandlers extends CreatesCoreComponentsBootstrapperPhase with HasCommandHandlerRegistry{ self: HasServiceRegistry =>
  override def commandHandlerRegistry: HasCommandHandlers = {
    if(myCommandHandlerRegistry == null)
      throw new Exception("You are trying to access the CommandHandlerRegistry. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myCommandHandlerRegistry
  }
  private var myCommandHandlerRegistry: HasCommandHandlers = null

  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      startUpLogger.info(s"Create HasCommandHandlers")
      myCommandHandlerRegistry = HasCommandHandlers()
      startUpLogger.info(s"Register HasCommandHandlers")
      self.serviceRegistry.registerService[HasCommandHandlers](myCommandHandlerRegistry)
      BootstrapperPhaseSuccess()
    }
}