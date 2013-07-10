package almhirt.common

import org.joda.time.DateTime

trait CanCreateUuid { def getUuid(): java.util.UUID }

trait CanCreateDateTime { def getDateTime(): DateTime }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime

object CanCreateUuidsAndDateTimes {
  def apply(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getDateTime(): DateTime = org.joda.time.DateTime.now()
  }

  def utc(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getDateTime(): DateTime = org.joda.time.DateTime.now()
  }

}