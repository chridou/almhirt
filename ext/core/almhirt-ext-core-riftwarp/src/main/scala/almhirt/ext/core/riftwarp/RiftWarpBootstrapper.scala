package almhirt.ext.core.riftwarp

import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core.ServiceRegistry
import almhirt.environment._
import almhirt.ext.core.riftwarp.serialization.RiftWarpUtilityFuns._
import almhirt.environment.configuration.CleanUpAction
import riftwarp._

trait RiftWarpBootstrapper { self: almhirt.environment.configuration.AlmhirtBootstrapper =>
  override def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = {
    val riftwarp = RiftWarp.concurrentWithDefaults
    addRiftWarpRegistrations(riftwarp)
    theServiceRegistry.registerService[RiftWarp](riftwarp)
    self.createCoreComponents(theAlmhirt,theServiceRegistry, startUpLogger)
  }
}