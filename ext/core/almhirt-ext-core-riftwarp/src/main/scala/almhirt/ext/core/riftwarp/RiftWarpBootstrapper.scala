package almhirt.ext.core.riftwarp

import almhirt.common._
import almhirt.environment._
import riftwarp._
import almhirt.ext.core.riftwarp.serialization.RiftWarpUtilityFuns._
import almhirt.environment.configuration.CleanUpAction
import ch.qos.logback.classic.Logger

trait RiftWarpBootstrapper { self: almhirt.environment.configuration.AlmhirtBootstrapper =>
  override def createCoreComponents(implicit theAlmhirt: Almhirt, startUpLogger: Logger): AlmValidation[CleanUpAction] = {
    theAlmhirt.serviceRegistry match {
      case Some(sr) =>
        val riftwarp = RiftWarp.concurrentWithDefaults
        addRiftWarpRegistrations(riftwarp)
        sr.registerService[RiftWarp](riftwarp)
        self.createCoreComponents(theAlmhirt)
      case None => scalaz.Failure(UnspecifiedProblem("Cannot create almhirt without a service registry"))
    }
  }
}