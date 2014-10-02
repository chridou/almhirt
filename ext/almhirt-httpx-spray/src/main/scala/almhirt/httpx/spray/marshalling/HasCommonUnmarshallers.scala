package almhirt.httpx.spray.marshalling

import almhirt.http.HasCommonHttpSerializers
import spray.httpx.unmarshalling.Unmarshaller

trait HasCommonUnmarshallers {
  implicit def booleanUnmarshaller: Unmarshaller[Boolean]
  implicit def stringUnmarshaller: Unmarshaller[String]
  implicit def byteUnmarshaller: Unmarshaller[Byte]
  implicit def shortUnmarshaller: Unmarshaller[Short]
  implicit def intUnmarshaller: Unmarshaller[Int]
  implicit def longUnmarshaller: Unmarshaller[Long]
  implicit def bigIntUnmarshaller: Unmarshaller[BigInt]
  implicit def floatUnmarshaller: Unmarshaller[Float]
  implicit def doubleUnmarshaller: Unmarshaller[Double]
  implicit def bigDecimalUnmarshaller: Unmarshaller[BigDecimal]
  implicit def uriUnmarshaller: Unmarshaller[java.net.URI]
  implicit def uuidUnmarshaller: Unmarshaller[java.util.UUID]
  implicit def localDateTimeUnmarshaller: Unmarshaller[org.joda.time.LocalDateTime]
  implicit def dateTimeUnmarshaller: Unmarshaller[org.joda.time.DateTime]
  implicit def finiteDurationUnmarshaller: Unmarshaller[scala.concurrent.duration.FiniteDuration]

  implicit def booleansUnmarshaller: Unmarshaller[Seq[Boolean]]
  implicit def stringsUnmarshaller: Unmarshaller[Seq[String]]
  implicit def bytesUnmarshaller: Unmarshaller[Seq[Byte]]
  implicit def shortsUnmarshaller: Unmarshaller[Seq[Short]]
  implicit def intsUnmarshaller: Unmarshaller[Seq[Int]]
  implicit def longsUnmarshaller: Unmarshaller[Seq[Long]]
  implicit def bigIntsUnmarshaller: Unmarshaller[Seq[BigInt]]
  implicit def floatsUnmarshaller: Unmarshaller[Seq[Float]]
  implicit def doublesUnmarshaller: Unmarshaller[Seq[Double]]
  implicit def bigDecimalsUnmarshaller: Unmarshaller[Seq[BigDecimal]]
  implicit def urisUnmarshaller: Unmarshaller[Seq[java.net.URI]]
  implicit def uuidsUnmarshaller: Unmarshaller[Seq[java.util.UUID]]
  implicit def localDateTimesUnmarshaller: Unmarshaller[Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesUnmarshaller: Unmarshaller[Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsUnmarshaller: Unmarshaller[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventUnmarshaller: Unmarshaller[almhirt.common.Event]
  def systemEventUnmarshaller: Unmarshaller[almhirt.common.SystemEvent]
  def domainEventUnmarshaller: Unmarshaller[almhirt.common.DomainEvent]
  def aggregateRootEventUnmarshaller: Unmarshaller[almhirt.common.AggregateRootEvent]
  implicit def commandUnmarshaller: Unmarshaller[almhirt.common.Command]
  implicit def problemUnmarshaller: Unmarshaller[almhirt.common.Problem]
  implicit def commandResponseUnmarshaller: Unmarshaller[almhirt.tracking.CommandResponse]

  implicit def eventsUnmarshaller: Unmarshaller[Seq[almhirt.common.Event]]
  implicit def systemEventsUnmarshaller: Unmarshaller[Seq[almhirt.common.SystemEvent]]
  implicit def domainEventsUnmarshaller: Unmarshaller[Seq[almhirt.common.DomainEvent]]
  implicit def aggregateRootEventsUnmarshaller: Unmarshaller[Seq[almhirt.common.AggregateRootEvent]]
  implicit def commandsUnmarshaller: Unmarshaller[Seq[almhirt.common.Command]]
  implicit def problemsUnmarshaller: Unmarshaller[Seq[almhirt.common.Problem]]
}

trait CommonUnmarshallerInstances { self: HasCommonUnmarshallers ⇒
  def commonHttpSerializers: HasCommonHttpSerializers
  def commonContentTypeProviders: HasCommonContentTypeProviders

  override lazy val booleanUnmarshaller: Unmarshaller[Boolean] =
    ContentTypeBoundUnmarshallerFactory[Boolean](commonContentTypeProviders.booleanContentTypeProvider, DefaultMarshallingInstances.BooleanMarshallingInst).unmarshaller(commonHttpSerializers.booleanHttpSerializer)

  override lazy val stringUnmarshaller: Unmarshaller[String] =
    ContentTypeBoundUnmarshallerFactory[String](commonContentTypeProviders.stringContentTypeProvider, DefaultMarshallingInstances.StringMarshallingInst).unmarshaller(commonHttpSerializers.stringHttpSerializer)

  override lazy val byteUnmarshaller: Unmarshaller[Byte] =
    ContentTypeBoundUnmarshallerFactory[Byte](commonContentTypeProviders.byteContentTypeProvider, DefaultMarshallingInstances.ByteMarshallingInst).unmarshaller(commonHttpSerializers.byteHttpSerializer)

  override lazy val shortUnmarshaller: Unmarshaller[Short] =
    ContentTypeBoundUnmarshallerFactory[Short](commonContentTypeProviders.shortContentTypeProvider, DefaultMarshallingInstances.ShortMarshallingInst).unmarshaller(commonHttpSerializers.shortHttpSerializer)

  override lazy val intUnmarshaller: Unmarshaller[Int] =
    ContentTypeBoundUnmarshallerFactory[Int](commonContentTypeProviders.intContentTypeProvider, DefaultMarshallingInstances.IntMarshallingInst).unmarshaller(commonHttpSerializers.intHttpSerializer)

  override lazy val longUnmarshaller: Unmarshaller[Long] =
    ContentTypeBoundUnmarshallerFactory[Long](commonContentTypeProviders.longContentTypeProvider, DefaultMarshallingInstances.LongMarshallingInst).unmarshaller(commonHttpSerializers.longHttpSerializer)

  override lazy val bigIntUnmarshaller: Unmarshaller[BigInt] =
    ContentTypeBoundUnmarshallerFactory[BigInt](commonContentTypeProviders.bigIntContentTypeProvider, DefaultMarshallingInstances.BigIntMarshallingInst).unmarshaller(commonHttpSerializers.bigIntHttpSerializer)

  override lazy val floatUnmarshaller: Unmarshaller[Float] =
    ContentTypeBoundUnmarshallerFactory[Float](commonContentTypeProviders.floatContentTypeProvider, DefaultMarshallingInstances.FloatMarshallingInst).unmarshaller(commonHttpSerializers.floatHttpSerializer)

  override lazy val doubleUnmarshaller: Unmarshaller[Double] =
    ContentTypeBoundUnmarshallerFactory[Double](commonContentTypeProviders.doubleContentTypeProvider, DefaultMarshallingInstances.DoubleMarshallingInst).unmarshaller(commonHttpSerializers.doubleHttpSerializer)

  override lazy val bigDecimalUnmarshaller: Unmarshaller[BigDecimal] =
    ContentTypeBoundUnmarshallerFactory[BigDecimal](commonContentTypeProviders.bigDecimalContentTypeProvider, DefaultMarshallingInstances.BigDecimalMarshallingInst).unmarshaller(commonHttpSerializers.bigDecimalHttpSerializer)

  override lazy val uriUnmarshaller: Unmarshaller[java.net.URI] =
    ContentTypeBoundUnmarshallerFactory[java.net.URI](commonContentTypeProviders.uriContentTypeProvider, DefaultMarshallingInstances.UriMarshallingInst).unmarshaller(commonHttpSerializers.uriHttpSerializer)

  override lazy val uuidUnmarshaller: Unmarshaller[java.util.UUID] =
    ContentTypeBoundUnmarshallerFactory[java.util.UUID](commonContentTypeProviders.uuidContentTypeProvider, DefaultMarshallingInstances.UuidMarshallingInst).unmarshaller(commonHttpSerializers.uuidHttpSerializer)

  override lazy val localDateTimeUnmarshaller: Unmarshaller[org.joda.time.LocalDateTime] =
    ContentTypeBoundUnmarshallerFactory[org.joda.time.LocalDateTime](commonContentTypeProviders.localDateTimeContentTypeProvider, DefaultMarshallingInstances.LocalDateTimeMarshallingInst).unmarshaller(commonHttpSerializers.localDateTimeHttpSerializer)

  override lazy val dateTimeUnmarshaller: Unmarshaller[org.joda.time.DateTime] =
    ContentTypeBoundUnmarshallerFactory[org.joda.time.DateTime](commonContentTypeProviders.dateTimeContentTypeProvider, DefaultMarshallingInstances.DateTimeMarshallingInst).unmarshaller(commonHttpSerializers.dateTimeHttpSerializer)

  override lazy val finiteDurationUnmarshaller: Unmarshaller[scala.concurrent.duration.FiniteDuration] =
    ContentTypeBoundUnmarshallerFactory[scala.concurrent.duration.FiniteDuration](commonContentTypeProviders.finiteDurationTypeProvider, DefaultMarshallingInstances.DurationMarshallingInst).unmarshaller(commonHttpSerializers.finiteDurationHttpSerializer)

  override lazy val booleansUnmarshaller: Unmarshaller[Seq[Boolean]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Boolean]](commonContentTypeProviders.booleansContentTypeProvider, DefaultMarshallingInstances.BooleansMarshallingInst).unmarshaller(commonHttpSerializers.booleansHttpSerializer)

  override lazy val stringsUnmarshaller: Unmarshaller[Seq[String]] =
    ContentTypeBoundUnmarshallerFactory[Seq[String]](commonContentTypeProviders.stringsContentTypeProvider, DefaultMarshallingInstances.StringsMarshallingInst).unmarshaller(commonHttpSerializers.stringsHttpSerializer)

  override lazy val bytesUnmarshaller: Unmarshaller[Seq[Byte]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Byte]](commonContentTypeProviders.bytesContentTypeProvider, DefaultMarshallingInstances.BytesMarshallingInst).unmarshaller(commonHttpSerializers.bytesHttpSerializer)

  override lazy val shortsUnmarshaller: Unmarshaller[Seq[Short]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Short]](commonContentTypeProviders.shortsContentTypeProvider, DefaultMarshallingInstances.ShortsMarshallingInst).unmarshaller(commonHttpSerializers.shortsHttpSerializer)

  override lazy val intsUnmarshaller: Unmarshaller[Seq[Int]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Int]](commonContentTypeProviders.intsContentTypeProvider, DefaultMarshallingInstances.IntsMarshallingInst).unmarshaller(commonHttpSerializers.intsHttpSerializer)

  override lazy val longsUnmarshaller: Unmarshaller[Seq[Long]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Long]](commonContentTypeProviders.longsContentTypeProvider, DefaultMarshallingInstances.LongsMarshallingInst).unmarshaller(commonHttpSerializers.longsHttpSerializer)

  override lazy val bigIntsUnmarshaller: Unmarshaller[Seq[BigInt]] =
    ContentTypeBoundUnmarshallerFactory[Seq[BigInt]](commonContentTypeProviders.bigIntsContentTypeProvider, DefaultMarshallingInstances.BigIntsMarshallingInst).unmarshaller(commonHttpSerializers.bigIntsHttpSerializer)

  override lazy val floatsUnmarshaller: Unmarshaller[Seq[Float]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Float]](commonContentTypeProviders.floatsContentTypeProvider, DefaultMarshallingInstances.FloatsMarshallingInst).unmarshaller(commonHttpSerializers.floatsHttpSerializer)

  override lazy val doublesUnmarshaller: Unmarshaller[Seq[Double]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Double]](commonContentTypeProviders.doublesContentTypeProvider, DefaultMarshallingInstances.DoublesMarshallingInst).unmarshaller(commonHttpSerializers.doublesHttpSerializer)

  override lazy val bigDecimalsUnmarshaller: Unmarshaller[Seq[BigDecimal]] =
    ContentTypeBoundUnmarshallerFactory[Seq[BigDecimal]](commonContentTypeProviders.bigDecimalsContentTypeProvider, DefaultMarshallingInstances.BigDecimalsMarshallingInst).unmarshaller(commonHttpSerializers.bigDecimalsHttpSerializer)

  override lazy val urisUnmarshaller: Unmarshaller[Seq[java.net.URI]] =
    ContentTypeBoundUnmarshallerFactory[Seq[java.net.URI]](commonContentTypeProviders.urisContentTypeProvider, DefaultMarshallingInstances.UrisMarshallingInst).unmarshaller(commonHttpSerializers.urisHttpSerializer)

  override lazy val uuidsUnmarshaller: Unmarshaller[Seq[java.util.UUID]] =
    ContentTypeBoundUnmarshallerFactory[Seq[java.util.UUID]](commonContentTypeProviders.uuidsContentTypeProvider, DefaultMarshallingInstances.UuidsMarshallingInst).unmarshaller(commonHttpSerializers.uuidsHttpSerializer)

  override lazy val localDateTimesUnmarshaller: Unmarshaller[Seq[org.joda.time.LocalDateTime]] =
    ContentTypeBoundUnmarshallerFactory[Seq[org.joda.time.LocalDateTime]](commonContentTypeProviders.localDateTimesContentTypeProvider, DefaultMarshallingInstances.LocalDateTimesMarshallingInst).unmarshaller(commonHttpSerializers.localDateTimesHttpSerializer)

  override lazy val dateTimesUnmarshaller: Unmarshaller[Seq[org.joda.time.DateTime]] =
    ContentTypeBoundUnmarshallerFactory[Seq[org.joda.time.DateTime]](commonContentTypeProviders.dateTimesContentTypeProvider, DefaultMarshallingInstances.DateTimesMarshallingInst).unmarshaller(commonHttpSerializers.dateTimesHttpSerializer)

  override lazy val finiteDurationsUnmarshaller: Unmarshaller[Seq[scala.concurrent.duration.FiniteDuration]] =
    ContentTypeBoundUnmarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]](commonContentTypeProviders.finiteDurationsContentTypeProvider, DefaultMarshallingInstances.DurationsMarshallingInst).unmarshaller(commonHttpSerializers.finiteDurationsHttpSerializer)

  override lazy val eventUnmarshaller: Unmarshaller[almhirt.common.Event] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Event](commonContentTypeProviders.eventContentTypeProvider, DefaultMarshallingInstances.EventMarshallingInst).unmarshaller(commonHttpSerializers.eventHttpSerializer)

  override lazy val systemEventUnmarshaller: Unmarshaller[almhirt.common.SystemEvent] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.SystemEvent](commonContentTypeProviders.systemEventContentTypeProvider, DefaultMarshallingInstances.SystemEventMarshallingInst).unmarshaller(commonHttpSerializers.systemEventHttpSerializer)

  override lazy val domainEventUnmarshaller: Unmarshaller[almhirt.common.DomainEvent] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.DomainEvent](commonContentTypeProviders.domainEventContentTypeProvider, DefaultMarshallingInstances.DomainEventMarshallingInst).unmarshaller(commonHttpSerializers.domainEventHttpSerializer)

  override lazy val aggregateRootEventUnmarshaller: Unmarshaller[almhirt.common.AggregateRootEvent] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.AggregateRootEvent](commonContentTypeProviders.aggregateRootEventContentTypeProvider, DefaultMarshallingInstances.AggregateRootEventMarshallingInst).unmarshaller(commonHttpSerializers.aggregateRootEventHttpSerializer)

  override lazy val commandUnmarshaller: Unmarshaller[almhirt.common.Command] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Command](commonContentTypeProviders.commandContentTypeProvider, DefaultMarshallingInstances.CommandMarshallingInst).unmarshaller(commonHttpSerializers.commandHttpSerializer)

  override lazy val problemUnmarshaller: Unmarshaller[almhirt.common.Problem] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Problem](commonContentTypeProviders.problemContentTypeProvider, DefaultMarshallingInstances.ProblemMarshallingInst).unmarshaller(commonHttpSerializers.problemHttpSerializer)

  override lazy val commandResponseUnmarshaller: Unmarshaller[almhirt.tracking.CommandResponse] =
    ContentTypeBoundUnmarshallerFactory[almhirt.tracking.CommandResponse](commonContentTypeProviders.commandResponseContentTypeProvider, DefaultMarshallingInstances.CommandResponseMarshallingInst).unmarshaller(commonHttpSerializers.commandResponseHttpSerializer)

  override lazy val eventsUnmarshaller: Unmarshaller[Seq[almhirt.common.Event]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Event]](commonContentTypeProviders.eventsContentTypeProvider, DefaultMarshallingInstances.EventsMarshallingInst).unmarshaller(commonHttpSerializers.eventsHttpSerializer)

  override lazy val systemEventsUnmarshaller: Unmarshaller[Seq[almhirt.common.SystemEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.SystemEvent]](commonContentTypeProviders.systemEventsContentTypeProvider, DefaultMarshallingInstances.SystemEventsMarshallingInst ).unmarshaller(commonHttpSerializers.systemEventsHttpSerializer)

  override lazy val domainEventsUnmarshaller: Unmarshaller[Seq[almhirt.common.DomainEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.DomainEvent]](commonContentTypeProviders.domainEventsContentTypeProvider, DefaultMarshallingInstances.DomainEventsMarshallingInst ).unmarshaller(commonHttpSerializers.domainEventsHttpSerializer)

  override lazy val aggregateRootEventsUnmarshaller: Unmarshaller[Seq[almhirt.common.AggregateRootEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.AggregateRootEvent]](commonContentTypeProviders.aggregateRootEventsContentTypeProvider, DefaultMarshallingInstances.AggregateRootEventsMarshallingInst ).unmarshaller(commonHttpSerializers.aggregateRootEventsHttpSerializer)

    
  override lazy val commandsUnmarshaller: Unmarshaller[Seq[almhirt.common.Command]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Command]](commonContentTypeProviders.commandsContentTypeProvider, DefaultMarshallingInstances.CommandsMarshallingInst).unmarshaller(commonHttpSerializers.commandsHttpSerializer)

  override lazy val problemsUnmarshaller: Unmarshaller[Seq[almhirt.common.Problem]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Problem]](commonContentTypeProviders.problemsContentTypeProvider, DefaultMarshallingInstances.ProblemsMarshallingInst).unmarshaller(commonHttpSerializers.problemsHttpSerializer)

}