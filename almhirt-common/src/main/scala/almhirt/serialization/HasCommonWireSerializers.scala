package almhirt.serialization

import almhirt.common.Event
import almhirt.common.Command

trait HasCommonWireSerializers {
  implicit def booleanWireSerializer: WireSerializer[Boolean, Boolean]
  implicit def stringWireSerializer: WireSerializer[String, String]
  implicit def byteWireSerializer: WireSerializer[Byte, Byte]
  implicit def shortWireSerializer: WireSerializer[Short, Short]
  implicit def intWireSerializer: WireSerializer[Int, Int]
  implicit def longWireSerializer: WireSerializer[Long, Long]
  implicit def bigIntWireSerializer: WireSerializer[BigInt, BigInt]
  implicit def floatWireSerializer: WireSerializer[Float, Float]
  implicit def doubleWireSerializer: WireSerializer[Double, Double]
  implicit def bigDecimalWireSerializer: WireSerializer[BigDecimal, BigDecimal]
  implicit def uriWireSerializer: WireSerializer[java.net.URI, java.net.URI]
  implicit def uuidWireSerializer: WireSerializer[java.util.UUID, java.util.UUID]
  implicit def localDateTimeWireSerializer: WireSerializer[org.joda.time.LocalDateTime, org.joda.time.LocalDateTime]
  implicit def dateTimeWireSerializer: WireSerializer[org.joda.time.DateTime, org.joda.time.DateTime]
  implicit def finiteDurationWireSerializer: WireSerializer[scala.concurrent.duration.FiniteDuration, scala.concurrent.duration.FiniteDuration]

  implicit def booleansWireSerializer: WireSerializer[Seq[Boolean], Seq[Boolean]]
  implicit def stringsWireSerializer: WireSerializer[Seq[String], Seq[String]]
  implicit def bytesWireSerializer: WireSerializer[Seq[Byte], Seq[Byte]]
  implicit def shortsWireSerializer: WireSerializer[Seq[Short], Seq[Short]]
  implicit def intsWireSerializer: WireSerializer[Seq[Int], Seq[Int]]
  implicit def longsWireSerializer: WireSerializer[Seq[Long], Seq[Long]]
  implicit def bigIntsWireSerializer: WireSerializer[Seq[BigInt], Seq[BigInt]]
  implicit def floatsWireSerializer: WireSerializer[Seq[Float], Seq[Float]]
  implicit def doublesWireSerializer: WireSerializer[Seq[Double], Seq[Double]]
  implicit def bigDecimalsWireSerializer: WireSerializer[Seq[BigDecimal], Seq[BigDecimal]]
  implicit def urisWireSerializer: WireSerializer[Seq[java.net.URI], Seq[java.net.URI]]
  implicit def uuidsWireSerializer: WireSerializer[Seq[java.util.UUID], Seq[java.util.UUID]]
  implicit def localDateTimesWireSerializer: WireSerializer[Seq[org.joda.time.LocalDateTime], Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesWireSerializer: WireSerializer[Seq[org.joda.time.DateTime], Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsWireSerializer: WireSerializer[Seq[scala.concurrent.duration.FiniteDuration], Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventWireSerializer: WireSerializer[Event, Event]
  implicit def commandWireSerializer: WireSerializer[Command, Command]
  implicit def problemWireSerializer: WireSerializer[almhirt.common.Problem, almhirt.common.Problem]

  implicit def eventsWireSerializer: WireSerializer[Seq[Event], Seq[Event]]
  implicit def commandsWireSerializer: WireSerializer[Seq[Command], Seq[Command]]
  implicit def problemsWireSerializer: WireSerializer[Seq[almhirt.common.Problem], Seq[almhirt.common.Problem]]

}