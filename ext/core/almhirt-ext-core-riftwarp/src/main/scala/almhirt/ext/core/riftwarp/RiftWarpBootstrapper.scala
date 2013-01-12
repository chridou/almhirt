package almhirt.ext.core.riftwarp

import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.environment._
import almhirt.ext.core.riftwarp.serialization.RiftWarpUtilityFuns._
import almhirt.environment.configuration.CleanUpAction
import riftwarp._

trait RiftWarpBootstrapper { self: almhirt.environment.configuration.AlmhirtBootstrapper =>
  override def createCoreComponents(implicit theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = {
    theAlmhirt.serviceRegistry match {
      case Some(sr) =>
        val riftwarp = RiftWarp.concurrentWithDefaults
        addRiftWarpRegistrations(riftwarp)
        sr.registerService[RiftWarp](riftwarp)
        self.createCoreComponents(theAlmhirt, startUpLogger)
      case None => scalaz.Failure(UnspecifiedProblem("Cannot create almhirt without a service registry"))
    }
  }
}