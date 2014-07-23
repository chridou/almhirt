package almhirt.http

trait HasCommonHttpSerializer[] with HttpDeserializers {
  implicit def booleanHttpSerializer[] with HttpDeserializer: HttpSerializer[Boolean] with HttpDeserializer[Boolean]
  implicit def stringHttpSerializer[] with HttpDeserializer: HttpSerializer[String] with HttpDeserializer[String]
  implicit def byteHttpSerializer[] with HttpDeserializer: HttpSerializer[Byte] with HttpDeserializer[Byte]
  implicit def shortHttpSerializer[] with HttpDeserializer: HttpSerializer[Short] with HttpDeserializer[Short]
  implicit def intHttpSerializer[] with HttpDeserializer: HttpSerializer[Int] with HttpDeserializer[Int]
  implicit def longHttpSerializer[] with HttpDeserializer: HttpSerializer[Long] with HttpDeserializer[Long]
  implicit def bigIntHttpSerializer[] with HttpDeserializer: HttpSerializer[BigInt] with HttpDeserializer[BigInt]
  implicit def floatHttpSerializer[] with HttpDeserializer: HttpSerializer[Float] with HttpDeserializer[Float]
  implicit def doubleHttpSerializer[] with HttpDeserializer: HttpSerializer[Double] with HttpDeserializer[Double]
  implicit def bigDecimalHttpSerializer[] with HttpDeserializer: HttpSerializer[BigDecimal] with HttpDeserializer[BigDecimal]
  implicit def uriHttpSerializer[] with HttpDeserializer: HttpSerializer[java.net.URI] with HttpDeserializer[java.net.URI]
  implicit def uuidHttpSerializer[] with HttpDeserializer: HttpSerializer[java.util.UUID] with HttpDeserializer[java.util.UUID]
  implicit def localDateTimeHttpSerializer[] with HttpDeserializer: HttpSerializer[org.joda.time.LocalDateTime] with HttpDeserializer[org.joda.time.LocalDateTime]
  implicit def dateTimeHttpSerializer[] with HttpDeserializer: HttpSerializer[org.joda.time.DateTime] with HttpDeserializer[org.joda.time.DateTime]
  implicit def finiteDurationHttpSerializer[] with HttpDeserializer: HttpSerializer[scala.concurrent.duration.FiniteDuration] with HttpDeserializer[scala.concurrent.duration.FiniteDuration]

  implicit def booleansHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Boolean]] with HttpDeserializer[Seq[Boolean]]
  implicit def stringsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[String]] with HttpDeserializer[Seq[String]]
  implicit def bytesHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Byte]] with HttpDeserializer[Seq[Byte]]
  implicit def shortsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Short]] with HttpDeserializer[Seq[Short]]
  implicit def intsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Int]] with HttpDeserializer[Seq[Int]]
  implicit def longsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Long]] with HttpDeserializer[Seq[Long]]
  implicit def bigIntsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[BigInt]] with HttpDeserializer[Seq[BigInt]]
  implicit def floatsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Float]] with HttpDeserializer[Seq[Float]]
  implicit def doublesHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Double]] with HttpDeserializer[Seq[Double]]
  implicit def bigDecimalsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[BigDecimal]] with HttpDeserializer[Seq[BigDecimal]]
  implicit def urisHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[java.net.URI]] with HttpDeserializer[Seq[java.net.URI]]
  implicit def uuidsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[java.util.UUID]] with HttpDeserializer[Seq[java.util.UUID]]
  implicit def localDateTimesHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[org.joda.time.LocalDateTime]] with HttpDeserializer[Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesHttpSerializer[] with HttpDeserializer: HttpSerializer[org.joda.time.DateTime] with HttpDeserializer[Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[scala.concurrent.duration.FiniteDuration]] with HttpDeserializer[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventHttpSerializer[] with HttpDeserializer: HttpSerializer[Event] with HttpDeserializer[Event]
  implicit def commandHttpSerializer[] with HttpDeserializer: HttpSerializer[Command] with HttpDeserializer[Command]
  implicit def problemHttpSerializer[] with HttpDeserializer: HttpSerializer[almhirt.common.Problem] with HttpDeserializer[almhirt.common.Problem]

  implicit def eventsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Event]] with HttpDeserializer[Seq[Event]]
  implicit def commandsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[Command]] with HttpDeserializer[Seq[Command]]
  implicit def problemsHttpSerializer[] with HttpDeserializer: HttpSerializer[Seq[almhirt.common.Problem]] with HttpDeserializer[Seq[almhirt.common.Problem]]

}