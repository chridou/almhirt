package almhirt.environment.configuration.bootstrappers

import almhirt.environment.configuration._
import akka.event.LoggingAdapter
import almhirt.core.HasConfig
import almhirt.core.HasServiceRegistry

trait RegistersConfiguration extends PreInitBootstrapperPhase { self: HasConfig with HasServiceRegistry =>
  override def preInit(startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.preInit(startUpLogger).andThen {
      self.serviceRegistry.registerService[HasConfig](new HasConfig { val config = self.config })
      BootstrapperPhaseSuccess()
    }
}