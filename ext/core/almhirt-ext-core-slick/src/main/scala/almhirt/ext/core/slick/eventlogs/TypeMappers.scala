package almhirt.ext.core.slick.eventlogs

import scalaz.syntax.validation._
import org.joda.time.{DateTime, DateTimeZone}
import almhirt.common._
import almhirt.almvalidation.kit._

trait SlickTypeMappers { this: Profile =>
  import profile.simple._
  import java.sql.Timestamp
  implicit val JodaTimeToSqlTimestampMapper: TypeMapper[DateTime] =
    MappedTypeMapper.base[DateTime, Timestamp](
      dateTime => new Timestamp(dateTime.getMillis),
      timestamp => new DateTime(timestamp.getTime, DateTimeZone.UTC))
}
