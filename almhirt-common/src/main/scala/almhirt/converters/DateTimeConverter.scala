package almhirt.converters

import java.time._

object DateTimeConverter {
  def localDateTimeToUtcEpochMillis(ldt: LocalDateTime): Long =
    ldt.toInstant(ZoneOffset.UTC).toEpochMilli()

  def utcEpochMillisToLocalDateTime(millis: Long): LocalDateTime = {
    val instant = Instant.ofEpochMilli(millis)
    LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
  }

  def localDateTimeToUtcZonedDateTime(ldt: LocalDateTime): ZonedDateTime =
    ldt.atOffset(ZoneOffset.UTC).toZonedDateTime()

  def zonedDateTimeToUtcLocalDateTime(ldt: ZonedDateTime): LocalDateTime =
    ldt.toLocalDateTime()
    
}