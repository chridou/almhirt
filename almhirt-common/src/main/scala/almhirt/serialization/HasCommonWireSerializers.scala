package almhirt.serialization

import almhirt.common.Event
import almhirt.common.Command

trait HasCommonWireSerializers {
  implicit def booleanWireSerializer: WireSerializer[Boolean]
  implicit def stringWireSerializer: WireSerializer[String]
  implicit def byteWireSerializer: WireSerializer[Byte]
  implicit def shortWireSerializer: WireSerializer[Short]
  implicit def intWireSerializer: WireSerializer[Int]
  implicit def longWireSerializer: WireSerializer[Long]
  implicit def bigIntWireSerializer: WireSerializer[BigInt]
  implicit def floatWireSerializer: WireSerializer[Float]
  implicit def doubleWireSerializer: WireSerializer[Double]
  implicit def bigDecimalWireSerializer: WireSerializer[BigDecimal]
  implicit def uriWireSerializer: WireSerializer[java.net.URI]
  implicit def uuidWireSerializer: WireSerializer[java.util.UUID]
  implicit def localDateTimeWireSerializer: WireSerializer[org.joda.time.LocalDateTime]
  implicit def dateTimeWireSerializer: WireSerializer[org.joda.time.DateTime]
  implicit def finiteDurationWireSerializer: WireSerializer[scala.concurrent.duration.FiniteDuration]

  implicit def booleansWireSerializer: WireSerializer[Seq[Boolean]]
  implicit def stringsWireSerializer: WireSerializer[Seq[String]]
  implicit def bytesWireSerializer: WireSerializer[Seq[Byte]]
  implicit def shortsWireSerializer: WireSerializer[Seq[Short]]
  implicit def intsWireSerializer: WireSerializer[Seq[Int]]
  implicit def longsWireSerializer: WireSerializer[Seq[Long]]
  implicit def bigIntsWireSerializer: WireSerializer[Seq[BigInt]]
  implicit def floatsWireSerializer: WireSerializer[Seq[Float]]
  implicit def doublesWireSerializer: WireSerializer[Seq[Double]]
  implicit def bigDecimalsWireSerializer: WireSerializer[Seq[BigDecimal]]
  implicit def urisWireSerializer: WireSerializer[Seq[java.net.URI]]
  implicit def uuidsWireSerializer: WireSerializer[Seq[java.util.UUID]]
  implicit def localDateTimesWireSerializer: WireSerializer[Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesWireSerializer: WireSerializer[Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsWireSerializer: WireSerializer[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventWireSerializer: WireSerializer[Event]
  implicit def commandWireSerializer: WireSerializer[Command]
  implicit def problemWireSerializer: WireSerializer[almhirt.common.Problem]

  implicit def eventsWireSerializer: WireSerializer[Seq[Event]]
  implicit def commandsWireSerializer: WireSerializer[Seq[Command]]
  implicit def problemsWireSerializer: WireSerializer[Seq[almhirt.common.Problem]]

}