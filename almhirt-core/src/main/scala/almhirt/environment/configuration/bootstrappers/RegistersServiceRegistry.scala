package almhirt.environment.configuration.bootstrappers

import almhirt.environment.configuration._
import akka.event.LoggingAdapter
import almhirt.core.HasServiceRegistry
import almhirt.core.ServiceRegistry

/**
 * This one registers the service registry to itself.
 */
trait RegistersServiceRegistry extends PreInitBootstrapperPhase { self: HasServiceRegistry =>
  override def preInit(startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.preInit(startUpLogger).andThen {
      self.serviceRegistry.registerService[ServiceRegistry](self.serviceRegistry)
      BootstrapperPhaseSuccess()
    }
}