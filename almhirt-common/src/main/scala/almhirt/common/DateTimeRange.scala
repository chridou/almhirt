package almhirt.common

import java.time.{ LocalDateTime, ZonedDateTime }

final case class LocalDateTimeRange(from: LocalDateTimeRange.RangeStart, to: LocalDateTimeRange.RangeEnd)

object LocalDateTimeRange {
  sealed trait RangeStart
  object BeginningOfTime extends RangeStart
  final case class From(when: LocalDateTime) extends RangeStart
  final case class After(when: LocalDateTime) extends RangeStart

  sealed trait RangeEnd
  object EndOfTime extends RangeEnd
  final case class Until(when: LocalDateTime) extends RangeEnd
  final case class To(when: LocalDateTime) extends RangeEnd

  implicit class RangeStartOps(self: RangeStart) {
    def until(end: LocalDateTime): LocalDateTimeRange = LocalDateTimeRange(self, Until(end))
    def to(end: LocalDateTime): LocalDateTimeRange = LocalDateTimeRange(self, To(end))
    def endless: LocalDateTimeRange = LocalDateTimeRange(self, EndOfTime)
  }
}

final case class DateTimeRange(from: DateTimeRange.RangeStart, to: DateTimeRange.RangeEnd)

object DateTimeRange {
  sealed trait RangeStart
  object BeginningOfTime extends RangeStart
  final case class From(when: ZonedDateTime) extends RangeStart
  final case class After(when: ZonedDateTime) extends RangeStart

  sealed trait RangeEnd
  object EndOfTime extends RangeEnd
  final case class Until(when: ZonedDateTime) extends RangeEnd
  final case class To(when: ZonedDateTime) extends RangeEnd

  implicit class RangeStartOps(self: RangeStart) {
    def until(end: ZonedDateTime): DateTimeRange = DateTimeRange(self, Until(end))
    def to(end: ZonedDateTime): DateTimeRange = DateTimeRange(self, To(end))
    def endless: DateTimeRange = DateTimeRange(self, EndOfTime)
  }

}