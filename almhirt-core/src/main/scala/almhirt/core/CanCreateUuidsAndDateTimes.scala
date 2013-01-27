package almhirt.core

import org.joda.time.DateTime
import almhirt.messaging._

trait CanCreateUuid{ def getUuid(): java.util.UUID = java.util.UUID.randomUUID() }

trait CanCreateDateTime{ def getDateTime(): DateTime = org.joda.time.DateTime.now() }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime

object CanCreateUuidsAndDateTimes {
  implicit class CanCreateUuidsAndDateTimesOps(self: CanCreateUuidsAndDateTimes) {
	  def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = {
	    val header = MessageHeader(self.getUuid, None, Map.empty, self.getDateTime)
	    Message(header, payload)
	  }
  }
}