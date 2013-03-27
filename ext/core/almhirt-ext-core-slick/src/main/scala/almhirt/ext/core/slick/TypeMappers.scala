package almhirt.ext.core.slick

import scalaz.syntax.validation._
import org.joda.time.{DateTime, DateTimeZone}
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.eventlogs.Profile
import java.sql.Timestamp

trait SlickTypeMappers { this: Profile =>
  import profile.simple._
  import java.sql.Timestamp
  implicit val JodaTimeToSqlTimestampMapper: TypeMapper[DateTime] =
    MappedTypeMapper.base[DateTime, Timestamp](
      dateTime => new Timestamp(dateTime.getMillis),
      timestamp => new DateTime(timestamp.getTime, DateTimeZone.UTC))
}

object TypeConversion {
  def dateTimeToTimeStamp(dateTime: DateTime): Timestamp = new Timestamp(dateTime.getMillis)
  def timestampToUtcDateTime(timestamp: Timestamp): DateTime = new DateTime(timestamp.getTime, DateTimeZone.UTC)
}
