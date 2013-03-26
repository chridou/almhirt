package almhirt.ext.core.unfiltered.configuration

import almhirt.core.Almhirt
import almhirt.core.ServiceRegistry
import akka.event.LoggingAdapter
import almhirt.common.AlmValidation
import almhirt.core.HasServiceRegistry
import almhirt.core.HasConfig

trait UnfilteredBootstrapperExtension extends UnfilteredBootstrapper { self: HasServiceRegistry with HasConfig =>
  override def collectPlans(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[List[unfiltered.netty.async.Plan]]
}