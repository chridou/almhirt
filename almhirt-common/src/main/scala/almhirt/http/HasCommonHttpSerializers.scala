package almhirt.http

import almhirt.common.{ DomainEvent, DomainCommand }

trait HasCommonHttpSerializers {
  implicit def booleanHttpSerializer: HttpSerializer[Boolean] with HttpDeserializer[Boolean]
  implicit def stringHttpSerializer: HttpSerializer[String] with HttpDeserializer[String]
  implicit def byteHttpSerializer: HttpSerializer[Byte] with HttpDeserializer[Byte]
  implicit def shortHttpSerializer: HttpSerializer[Short] with HttpDeserializer[Short]
  implicit def intHttpSerializer: HttpSerializer[Int] with HttpDeserializer[Int]
  implicit def longHttpSerializer: HttpSerializer[Long] with HttpDeserializer[Long]
  implicit def bigIntHttpSerializer: HttpSerializer[BigInt] with HttpDeserializer[BigInt]
  implicit def floatHttpSerializer: HttpSerializer[Float] with HttpDeserializer[Float]
  implicit def doubleHttpSerializer: HttpSerializer[Double] with HttpDeserializer[Double]
  implicit def bigDecimalHttpSerializer: HttpSerializer[BigDecimal] with HttpDeserializer[BigDecimal]
  implicit def uriHttpSerializer: HttpSerializer[java.net.URI] with HttpDeserializer[java.net.URI]
  implicit def uuidHttpSerializer: HttpSerializer[java.util.UUID] with HttpDeserializer[java.util.UUID]
  implicit def localDateTimeHttpSerializer: HttpSerializer[java.time.LocalDateTime] with HttpDeserializer[java.time.LocalDateTime]
  implicit def dateTimeHttpSerializer: HttpSerializer[java.time.ZonedDateTime] with HttpDeserializer[java.time.ZonedDateTime]
  implicit def finiteDurationHttpSerializer: HttpSerializer[scala.concurrent.duration.FiniteDuration] with HttpDeserializer[scala.concurrent.duration.FiniteDuration]

  implicit def booleansHttpSerializer: HttpSerializer[Seq[Boolean]] with HttpDeserializer[Seq[Boolean]]
  implicit def stringsHttpSerializer: HttpSerializer[Seq[String]] with HttpDeserializer[Seq[String]]
  implicit def bytesHttpSerializer: HttpSerializer[Seq[Byte]] with HttpDeserializer[Seq[Byte]]
  implicit def shortsHttpSerializer: HttpSerializer[Seq[Short]] with HttpDeserializer[Seq[Short]]
  implicit def intsHttpSerializer: HttpSerializer[Seq[Int]] with HttpDeserializer[Seq[Int]]
  implicit def longsHttpSerializer: HttpSerializer[Seq[Long]] with HttpDeserializer[Seq[Long]]
  implicit def bigIntsHttpSerializer: HttpSerializer[Seq[BigInt]] with HttpDeserializer[Seq[BigInt]]
  implicit def floatsHttpSerializer: HttpSerializer[Seq[Float]] with HttpDeserializer[Seq[Float]]
  implicit def doublesHttpSerializer: HttpSerializer[Seq[Double]] with HttpDeserializer[Seq[Double]]
  implicit def bigDecimalsHttpSerializer: HttpSerializer[Seq[BigDecimal]] with HttpDeserializer[Seq[BigDecimal]]
  implicit def urisHttpSerializer: HttpSerializer[Seq[java.net.URI]] with HttpDeserializer[Seq[java.net.URI]]
  implicit def uuidsHttpSerializer: HttpSerializer[Seq[java.util.UUID]] with HttpDeserializer[Seq[java.util.UUID]]
  implicit def localDateTimesHttpSerializer: HttpSerializer[Seq[java.time.LocalDateTime]] with HttpDeserializer[Seq[java.time.LocalDateTime]]
  implicit def dateTimesHttpSerializer: HttpSerializer[Seq[java.time.ZonedDateTime]] with HttpDeserializer[Seq[java.time.ZonedDateTime]]
  implicit def finiteDurationsHttpSerializer: HttpSerializer[Seq[scala.concurrent.duration.FiniteDuration]] with HttpDeserializer[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventHttpSerializer: HttpSerializer[almhirt.common.Event] with HttpDeserializer[almhirt.common.Event]
  def systemEventHttpSerializer: HttpSerializer[almhirt.common.SystemEvent] with HttpDeserializer[almhirt.common.SystemEvent]
  def domainEventHttpSerializer: HttpSerializer[almhirt.common.DomainEvent] with HttpDeserializer[almhirt.common.DomainEvent]
  def aggregateRootEventHttpSerializer: HttpSerializer[almhirt.common.AggregateRootEvent] with HttpDeserializer[almhirt.common.AggregateRootEvent]
  implicit def commandHttpSerializer: HttpSerializer[almhirt.common.Command] with HttpDeserializer[almhirt.common.Command]
  implicit def problemHttpSerializer: HttpSerializer[almhirt.common.Problem] with HttpDeserializer[almhirt.common.Problem]
  implicit def commandResponseHttpSerializer: HttpSerializer[almhirt.tracking.CommandResponse] with HttpDeserializer[almhirt.tracking.CommandResponse]

  implicit def eventsHttpSerializer: HttpSerializer[Seq[almhirt.common.Event]] with HttpDeserializer[Seq[almhirt.common.Event]]
  implicit def systemEventsHttpSerializer: HttpSerializer[Seq[almhirt.common.SystemEvent]] with HttpDeserializer[Seq[almhirt.common.SystemEvent]]
  implicit def domainEventsHttpSerializer: HttpSerializer[Seq[almhirt.common.DomainEvent]] with HttpDeserializer[Seq[almhirt.common.DomainEvent]]
  implicit def aggregateRootEventsHttpSerializer: HttpSerializer[Seq[almhirt.common.AggregateRootEvent]] with HttpDeserializer[Seq[almhirt.common.AggregateRootEvent]]
  implicit def commandsHttpSerializer: HttpSerializer[Seq[almhirt.common.Command]] with HttpDeserializer[Seq[almhirt.common.Command]]
  implicit def problemsHttpSerializer: HttpSerializer[Seq[almhirt.common.Problem]] with HttpDeserializer[Seq[almhirt.common.Problem]]

  implicit def paramsHttpSerializer: HttpSerializer[almhirt.configuration.Params] with HttpDeserializer[almhirt.configuration.Params]

}