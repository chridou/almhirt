package almhirt.ext.core.riftwarp

import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.core.ServiceRegistry
import almhirt.environment._
import almhirt.ext.core.riftwarp.serialization.RiftWarpUtilityFuns._
import almhirt.environment.configuration._
import riftwarp._
import almhirt.core.HasServiceRegistry

trait RiftWarpBootstrapper extends CreatesCoreComponentsBootstrapperPhase { self: HasServiceRegistry =>
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      startUpLogger.debug("Create and registter RiftWarp.")
      val riftWarp = RiftWarp()
      addRiftWarpRegistrations(riftWarp)
      self.serviceRegistry.registerService[RiftWarp](riftWarp)
      BootstrapperPhaseSuccess()
    }
}
