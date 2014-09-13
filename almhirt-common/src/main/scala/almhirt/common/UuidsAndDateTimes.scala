package almhirt.common

import scalaz._, Scalaz._
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

trait CanCreateUuid { def getUuid(): java.util.UUID; def getUniqueString(): String; def parseUuid(str: String): AlmValidation[java.util.UUID] }

trait CanCreateDateTime { def getDateTime(): DateTime; def getUtcTimestamp: LocalDateTime }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime

object CanCreateUuidsAndDateTimes {
  def apply(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = createUniqueString
    override def getDateTime(): DateTime = new DateTime()
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
    override def parseUuid(str: String): AlmValidation[java.util.UUID] = almhirt.almvalidation.funs.parseUuidAlm(str)
  }

  def utc(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = createUniqueString
    override def getDateTime(): DateTime = new DateTime(DateTimeZone.UTC)
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
    override def parseUuid(str: String): AlmValidation[java.util.UUID] = almhirt.almvalidation.funs.parseUuidAlm(str)
  }

  private val BASE64 = new org.apache.commons.codec.binary.Base64(true)
  @inline
  private def createUniqueString: String = {
    val b64Str = new String(BASE64.encode(almhirt.converters.BinaryConverter.uuidToBytes(java.util.UUID.randomUUID())))
    b64Str.substring(0, b64Str.length() - 2)
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