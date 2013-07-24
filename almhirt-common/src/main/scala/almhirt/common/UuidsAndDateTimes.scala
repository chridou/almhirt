package almhirt.common

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

trait CanCreateUuid { def getUuid(): java.util.UUID; def getUniqueString(): String }

trait CanCreateDateTime { def getDateTime(): DateTime; def getUtcTimestamp: LocalDateTime }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime

object CanCreateUuidsAndDateTimes {
  def apply(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = java.util.UUID.randomUUID().toString().filterNot(_ == '-')
    override def getDateTime(): DateTime = new DateTime()
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
  }

  def utc(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = java.util.UUID.randomUUID().toString().filterNot(_ == '-')
    override def getDateTime(): DateTime = new DateTime(DateTimeZone.UTC)
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
  }

}