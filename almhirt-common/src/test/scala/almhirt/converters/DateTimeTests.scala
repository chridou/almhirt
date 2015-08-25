package almhirt.converters

import java.time._
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import java.nio.ByteBuffer

class DateTimeTests extends FunSuite with Matchers {
  import DateTimeConverter._

  val epochZero = LocalDateTime.parse("1970-01-01T00:00")

  test("LocalDatetime(1970-01-01T00:00) should convert to 0 millis(UTC)") {
    val millis = localDateTimeToUtcEpochMillis(epochZero)
    millis should equal(0L)
  }

  test("LocalDatetime(1970-01-01T00:01) should convert to 1000 millis(UTC)") {
    val millis = localDateTimeToUtcEpochMillis(epochZero.plusSeconds(1))
    millis should equal(1000L)
  }
  
  test("0 millis(UTC) should convert to the correct LocalDateTime 1970-01-01T00:00") {
    val ldt = utcEpochMillisToLocalDateTime(0L)
    ldt should equal(epochZero)
  }

  test("1000 millis(UTC) should convert to the correct LocalDateTime 1970-01-01T00:01") {
    val ldt = utcEpochMillisToLocalDateTime(1000L)
    ldt should equal(epochZero.plusSeconds(1))
  }
  
  test("A LocalDateTime(1970-01-01T00:00) must be converted to the correct ZonedDateTime(UTC") {
    val expected = ZonedDateTime.parse("1970-01-01T00:00+00:00")
    val res = localDateTimeToUtcZonedDateTime(epochZero)
    res should equal(expected)
  }

  test("A LocalDateTime(1970-01-01T02:00) must be converted to the correct ZonedDateTime(UTC") {
    val expected = ZonedDateTime.parse("1970-01-01T02:00+00:00")
    val res = localDateTimeToUtcZonedDateTime(epochZero.plusHours(2))
    res should equal(expected)
  }

  test("A ZonedDateTime(UTC) must be converted to the correct LocalDateTime") {
    val sample = ZonedDateTime.parse("1970-01-01T00:00+00:00")
    val expected = LocalDateTime.parse("1970-01-01T00:00")
    zonedDateTimeToUtcLocalDateTime(sample) should equal(expected)
  }
 
  test("A ZonedDateTime(1970-01-01T02:00+00:00) must be converted to the correct LocalDateTime") {
    val sample = ZonedDateTime.parse("1970-01-01T02:00+00:00")
    val expected = LocalDateTime.parse("1970-01-01T02:00")
    zonedDateTimeToUtcLocalDateTime(sample) should equal(expected)
  }

  test("A ZonedDateTime(1970-01-01T02:00 Europe/Paris) must be converted to the correct LocalDateTime") {
    val sample = LocalDateTime.parse("1970-01-01T02:00").atZone(ZoneId.of("Europe/Paris"))
    info(sample.toString)
    val expected = LocalDateTime.parse("1970-01-01T01:00")
    zonedDateTimeToUtcLocalDateTime(sample) should equal(expected)
  }
  
  test("A ZonedDateTime(+2.0) must be converted to the correct LocalDateTime") {
    val sample = ZonedDateTime.parse("1970-01-01T02:00+02:00")
    val expected = LocalDateTime.parse("1970-01-01T00:00")
    zonedDateTimeToUtcLocalDateTime(sample) should equal(expected)
  }

  test("A ZonedDateTime(-2.0) must be converted to the correct LocalDateTime") {
    val sample = ZonedDateTime.parse("1970-01-01T04:00-02:00")
    val expected = LocalDateTime.parse("1970-01-01T06:00")
    zonedDateTimeToUtcLocalDateTime(sample) should equal(expected)
  }
  
}