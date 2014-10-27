package almhirt.common

import scalaz._, Scalaz._
import org.joda.time.{ DateTime, DateTimeZone, LocalDateTime }
import scala.concurrent.duration.FiniteDuration

trait CanCreateUuid { def getUuid(): java.util.UUID; def getUniqueString(): String }

trait CanCreateDateTime { def getDateTime(): DateTime; def getUtcTimestamp: LocalDateTime }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime

object CanCreateUuidsAndDateTimes {
  def apply(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = almhirt.converters.MiscConverters.uuidToBase64String(java.util.UUID.randomUUID())
    override def getDateTime(): DateTime = new DateTime()
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
  }

  def utc(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = almhirt.converters.MiscConverters.uuidToBase64String(java.util.UUID.randomUUID())
    override def getDateTime(): DateTime = new DateTime(DateTimeZone.UTC)
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
  }

  def timeShifted(timeShift: FiniteDuration): CanCreateUuidsAndDateTimes = {
    val timeshiftMillis = timeShift.toMillis.toInt
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
      override def getUniqueString(): String = almhirt.converters.MiscConverters.uuidToBase64String(java.util.UUID.randomUUID())
      override def getDateTime(): DateTime = new DateTime(DateTimeZone.UTC).plusMillis(timeshiftMillis)
      override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC).plusMillis(timeshiftMillis)
    }
  }

  private val regexStr = """(?:[-\w:@&=+,.!~*'_;]|%\p{XDigit}{2})(?:[-\w:@&=+,.!~*'$_;]|%\p{XDigit}{2})*"""
  private val regex = regexStr.r
  def validateUniqueStringId(str: String): AlmValidation[String] =
    if ((regex findFirstIn str).nonEmpty) {
      str.success
    } else {
      BadDataProblem(s""""$id" is not a valid id. It must conform to the regular expression "$regexStr".""").failure
    }
}