package almhirt

import scala.language.implicitConversions 

package object tracking {
  implicit def tt2String(self: TrackingTicket): String = self.value 
  implicit def corId2String(self: CorrelationId): String = self.value 
}