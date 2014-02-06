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
    override def getUniqueString(): String = createUniqueString
    override def getDateTime(): DateTime = new DateTime()
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
  }

  def utc(): CanCreateUuidsAndDateTimes = new CanCreateUuidsAndDateTimes {
    override def getUuid(): java.util.UUID = java.util.UUID.randomUUID()
    override def getUniqueString(): String = createUniqueString
    override def getDateTime(): DateTime = new DateTime(DateTimeZone.UTC)
    override def getUtcTimestamp(): LocalDateTime = new LocalDateTime(DateTimeZone.UTC)
  }

  def createUniqueString: String =
    java.util.UUID.randomUUID().toString().view.filterNot(_ == '-').map {
      case '0' => 'g'
      case '1' => 'h'
      case '2' => 'i'
      case '3' => 'j'
      case '4' => 'k'
      case '5' => 'l'
      case '6' => 'm'
      case '7' => 'n'
      case '8' => 'o'
      case '9' => 'p'
    }.toString

}