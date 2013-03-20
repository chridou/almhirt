package almhirt

import almhirt.common.CanCreateUuidsAndDateTimes
import almhirt.messaging.MessageHeader
import almhirt.messaging.Message

package object core {
  implicit class CanCreateUuidsAndDateTimesOps(self: CanCreateUuidsAndDateTimes) {
	  def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = {
	    val header = MessageHeader(self.getUuid, None, Map.empty, self.getDateTime)
	    Message(header, payload)
	  }
  }

}