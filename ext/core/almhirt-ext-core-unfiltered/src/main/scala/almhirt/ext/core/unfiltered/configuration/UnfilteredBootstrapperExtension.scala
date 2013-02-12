package almhirt.ext.core.unfiltered.configuration

import almhirt.core.Almhirt
import almhirt.core.ServiceRegistry
import akka.event.LoggingAdapter
import almhirt.common.AlmValidation

trait UnfilteredBootstrapperExtension extends UnfilteredBootstrapper {
  override def collectPlans(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[List[unfiltered.netty.async.Plan]]
}