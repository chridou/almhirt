package almhirt.ext.core.riftwarp

import almhirt.common._
import almhirt.environment._
import riftwarp._
import almhirt.ext.core.riftwarp.serialization.RiftWarpUtilityFuns._

trait RiftWarpBootstrapper { self: almhirt.environment.configuration.AlmhirtBootstrapper =>
  override def registerComponents(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit] = {
    val riftwarp = RiftWarp.concurrentWithDefaults
    addRiftWarpRegistrations(riftwarp)
    almhirt.registerService[RiftWarp](riftwarp)
    self.registerComponents(almhirt, context, system)
  }
}