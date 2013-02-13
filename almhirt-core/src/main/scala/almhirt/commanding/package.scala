package almhirt

import language.implicitConversions
import almhirt.common._
import almhirt.core._
import almhirt.domain._
import almhirt.common.AlmFuture
import almhirt.core.Almhirt
import almhirt.domain.AggregateRootRef

package object commanding {
  implicit def tupleToAggRef(t: (java.util.UUID, Long)): AggregateRootRef = AggregateRootRef(t._1, t._2)
}