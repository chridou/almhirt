package almhirt.common

import org.joda.time.DateTime

trait CanCreateUuid { def getUuid(): java.util.UUID = java.util.UUID.randomUUID() }

trait CanCreateDateTime { def getDateTime(): DateTime = org.joda.time.DateTime.now() }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime

object CanCreateUuidsAndDateTimes {
  def apply(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
     override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
     override def getDateTime(): DateTime = org.joda.time.DateTime.now()
  }
}