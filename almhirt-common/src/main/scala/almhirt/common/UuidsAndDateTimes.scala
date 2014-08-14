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

  @inline
  private def nibbleToChar(b: Int): Char =
    (b + 'a').toChar

  @inline
  private def addByte(b: Byte, builder: StringBuilder) {
     builder.append(nibbleToChar(b & 0x0F))
     builder.append(nibbleToChar((b & 0xF0) >> 4))
  }

  @inline
  def createUniqueString: String = {
    val builder = new StringBuilder()
    almhirt.converters.BinaryConverter.uuidToBytes(java.util.UUID.randomUUID()).foreach(b ⇒ addByte(b, builder))
    builder.toString
  }

}