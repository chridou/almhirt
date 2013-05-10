package almhirt.ext.core.unfiltered.configuration

import scalaz.syntax.validation._
import almhirt.core.Almhirt
import akka.event.LoggingAdapter
import almhirt.common.AlmValidation


trait UnfilteredBootstrapperPlansCollector  { 
  def collectPlans(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): AlmValidation[List[unfiltered.netty.async.Plan]] = 
    Nil.success
}