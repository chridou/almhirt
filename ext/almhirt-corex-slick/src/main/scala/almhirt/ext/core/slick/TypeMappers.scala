package almhirt.ext.core.slick

import language.implicitConversions
import scalaz.syntax.validation._
import org.joda.time.{LocalDateTime}
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared.Profile
import java.sql.Timestamp

trait SlickTypeMappers { this: Profile =>
  import profile.simple._
  import java.sql.Timestamp
  implicit val JodaLocalDateTimeTimeToSqlTimestampMapper: TypeMapper[LocalDateTime] =
    MappedTypeMapper.base[LocalDateTime, Timestamp](
      dateTime => new Timestamp(dateTime.toDateTime().getMillis()),
      timestamp => new LocalDateTime(timestamp.getTime))
}

object TypeConversion {
  implicit def dateTime2Timestamp(dateTime: LocalDateTime): Timestamp = new Timestamp(dateTime.toDateTime().getMillis())
  implicit def timestamp2UtcDateTime(timestamp: Timestamp): LocalDateTime = new LocalDateTime(timestamp.getTime)
}
