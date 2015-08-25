package almhirt.common

import scalaz._, Scalaz._
import _root_.java.time.{ ZonedDateTime, LocalDateTime, ZoneOffset }
import scala.concurrent.duration.FiniteDuration

trait CanCreateUuid { def getUuid(): java.util.UUID; def getUniqueString(): String }

trait CanCreateDateTime { def getDateTime(): ZonedDateTime; def getUtcTimestamp: LocalDateTime }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime

object CanCreateDateTime {
  implicit class CanCreateDateTimesOps(val self: CanCreateDateTime) extends AnyVal {
    def utcEpochMillis: Long = almhirt.converters.DateTimeConverter.localDateTimeToUtcEpochMillis(self.getUtcTimestamp)
  }
}

object CanCreateUuidsAndDateTimes {
  def apply(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = almhirt.converters.MiscConverters.uuidToBase64String(java.util.UUID.randomUUID())
    override def getDateTime(): ZonedDateTime = ZonedDateTime.now
    override def getUtcTimestamp(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
  }

  def utc(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = almhirt.converters.MiscConverters.uuidToBase64String(java.util.UUID.randomUUID())
    override def getDateTime(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
    override def getUtcTimestamp(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
  }

  def timeShifted(timeShift: FiniteDuration): CanCreateUuidsAndDateTimes = {
    val timeshiftSeconds = timeShift.toSeconds.toInt
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
      override def getUniqueString(): String = almhirt.converters.MiscConverters.uuidToBase64String(java.util.UUID.randomUUID())
      override def getDateTime(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(timeshiftSeconds)
      override def getUtcTimestamp(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(timeshiftSeconds)
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