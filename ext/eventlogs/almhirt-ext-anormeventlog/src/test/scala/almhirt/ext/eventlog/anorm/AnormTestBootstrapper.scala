package almhirt.ext.eventlog.anorm

import com.typesafe.config.Config
import almhirt.environment.configuration.impl.AlmhirtTestingBootstrapper
import almhirt.environment._
import almhirt.common._
import riftwarp.RiftWarp
import almhirt.environment.configuration.CleanUpAction

class AnormTestBootstrapper(config: Config) extends AlmhirtTestingBootstrapper(config) {
  override def createCoreComponents(implicit almhirt: Almhirt): AlmValidation[CleanUpAction] = {
    almhirt.serviceRegistry match {
      case Some(sr) =>
        val riftwarp = RiftWarp.concurrentWithDefaults
        val barracks = riftwarp.barracks

        barracks.addDecomposer(new TestPersonCreatedDecomposer)
        barracks.addDecomposer(new TestPersonNameChangedDecomposer)
        barracks.addDecomposer(new TestPersonAddressAquiredDecomposer)
        barracks.addDecomposer(new TestPersonMovedDecomposer)
        barracks.addDecomposer(new TestPersonUnhandledEventDecomposer)

        barracks.addRecomposer(new TestPersonCreatedRecomposer)
        barracks.addRecomposer(new TestPersonNameChangedRecomposer)
        barracks.addRecomposer(new TestPersonAddressAquiredRecomposer)
        barracks.addRecomposer(new TestPersonMovedRecomposer)
        barracks.addRecomposer(new TestPersonUnhandledEventRecomposer)

        sr.registerService[RiftWarp](riftwarp)
        super.createCoreComponents(almhirt)
      case None =>
        scalaz.Failure(UnspecifiedProblem("Cannot register services without a service registry"))
    }
  }
}