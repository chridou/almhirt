package almhirt.httpx.akkahttp.marshalling

import almhirt.http.HasCommonHttpSerializers
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import scala.concurrent.ExecutionContext
import akka.stream.Materializer

trait HasCommonUnmarshallers {
  implicit def booleanUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Boolean]
  implicit def stringUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[String]
  implicit def byteUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Byte]
  implicit def shortUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Short]
  implicit def intUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Int]
  implicit def longUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Long]
  implicit def bigIntUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[BigInt]
  implicit def floatUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Float]
  implicit def doubleUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Double]
  implicit def bigDecimalUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[BigDecimal]
  implicit def uriUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.net.URI]
  implicit def uuidUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.util.UUID]
  implicit def localDateTimeUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.time.LocalDateTime]
  implicit def dateTimeUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.time.ZonedDateTime]
  implicit def finiteDurationUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[scala.concurrent.duration.FiniteDuration]

  implicit def booleansUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Boolean]]
  implicit def stringsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[String]]
  implicit def bytesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Byte]]
  implicit def shortsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Short]]
  implicit def intsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Int]]
  implicit def longsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Long]]
  implicit def bigIntsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[BigInt]]
  implicit def floatsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Float]]
  implicit def doublesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Double]]
  implicit def bigDecimalsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[BigDecimal]]
  implicit def urisUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.net.URI]]
  implicit def uuidsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.util.UUID]]
  implicit def localDateTimesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.time.LocalDateTime]]
  implicit def dateTimesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.time.ZonedDateTime]]
  implicit def finiteDurationsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.Event]
  def systemEventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.SystemEvent]
  def domainEventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.DomainEvent]
  def aggregateRootEventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.AggregateRootEvent]
  implicit def commandUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.Command]
  implicit def problemUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.Problem]
  implicit def commandResponseUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.tracking.CommandResponse]

  implicit def eventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.Event]]
  implicit def systemEventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.SystemEvent]]
  implicit def domainEventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.DomainEvent]]
  implicit def aggregateRootEventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.AggregateRootEvent]]
  implicit def commandsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.Command]]
  implicit def problemsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.Problem]]
}

trait CommonUnmarshallerInstances { self: HasCommonUnmarshallers ⇒
  def commonHttpSerializers: HasCommonHttpSerializers
  def commonContentTypeProviders: HasCommonContentTypeProviders

  override def booleanUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Boolean] =
    ContentTypeBoundUnmarshallerFactory[Boolean](commonContentTypeProviders.booleanContentTypeProvider, DefaultMarshallingInstances.BooleanMarshallingInst).unmarshaller(commonHttpSerializers.booleanHttpSerializer, executionContext, materializer)

  override def stringUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[String] =
    ContentTypeBoundUnmarshallerFactory[String](commonContentTypeProviders.stringContentTypeProvider, DefaultMarshallingInstances.StringMarshallingInst).unmarshaller(commonHttpSerializers.stringHttpSerializer, executionContext, materializer)

  override def byteUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Byte] =
    ContentTypeBoundUnmarshallerFactory[Byte](commonContentTypeProviders.byteContentTypeProvider, DefaultMarshallingInstances.ByteMarshallingInst).unmarshaller(commonHttpSerializers.byteHttpSerializer, executionContext, materializer)

  override def shortUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Short] =
    ContentTypeBoundUnmarshallerFactory[Short](commonContentTypeProviders.shortContentTypeProvider, DefaultMarshallingInstances.ShortMarshallingInst).unmarshaller(commonHttpSerializers.shortHttpSerializer, executionContext, materializer)

  override def intUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Int] =
    ContentTypeBoundUnmarshallerFactory[Int](commonContentTypeProviders.intContentTypeProvider, DefaultMarshallingInstances.IntMarshallingInst).unmarshaller(commonHttpSerializers.intHttpSerializer, executionContext, materializer)

  override def longUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Long] =
    ContentTypeBoundUnmarshallerFactory[Long](commonContentTypeProviders.longContentTypeProvider, DefaultMarshallingInstances.LongMarshallingInst).unmarshaller(commonHttpSerializers.longHttpSerializer, executionContext, materializer)

  override def bigIntUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[BigInt] =
    ContentTypeBoundUnmarshallerFactory[BigInt](commonContentTypeProviders.bigIntContentTypeProvider, DefaultMarshallingInstances.BigIntMarshallingInst).unmarshaller(commonHttpSerializers.bigIntHttpSerializer, executionContext, materializer)

  override def floatUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Float] =
    ContentTypeBoundUnmarshallerFactory[Float](commonContentTypeProviders.floatContentTypeProvider, DefaultMarshallingInstances.FloatMarshallingInst).unmarshaller(commonHttpSerializers.floatHttpSerializer, executionContext, materializer)

  override def doubleUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Double] =
    ContentTypeBoundUnmarshallerFactory[Double](commonContentTypeProviders.doubleContentTypeProvider, DefaultMarshallingInstances.DoubleMarshallingInst).unmarshaller(commonHttpSerializers.doubleHttpSerializer, executionContext, materializer)

  override def bigDecimalUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[BigDecimal] =
    ContentTypeBoundUnmarshallerFactory[BigDecimal](commonContentTypeProviders.bigDecimalContentTypeProvider, DefaultMarshallingInstances.BigDecimalMarshallingInst).unmarshaller(commonHttpSerializers.bigDecimalHttpSerializer, executionContext, materializer)

  override def uriUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.net.URI] =
    ContentTypeBoundUnmarshallerFactory[java.net.URI](commonContentTypeProviders.uriContentTypeProvider, DefaultMarshallingInstances.UriMarshallingInst).unmarshaller(commonHttpSerializers.uriHttpSerializer, executionContext, materializer)

  override def uuidUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.util.UUID] =
    ContentTypeBoundUnmarshallerFactory[java.util.UUID](commonContentTypeProviders.uuidContentTypeProvider, DefaultMarshallingInstances.UuidMarshallingInst).unmarshaller(commonHttpSerializers.uuidHttpSerializer, executionContext, materializer)

  override def localDateTimeUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.time.LocalDateTime] =
    ContentTypeBoundUnmarshallerFactory[java.time.LocalDateTime](commonContentTypeProviders.localDateTimeContentTypeProvider, DefaultMarshallingInstances.LocalDateTimeMarshallingInst).unmarshaller(commonHttpSerializers.localDateTimeHttpSerializer, executionContext, materializer)

  override def dateTimeUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[java.time.ZonedDateTime] =
    ContentTypeBoundUnmarshallerFactory[java.time.ZonedDateTime](commonContentTypeProviders.dateTimeContentTypeProvider, DefaultMarshallingInstances.DateTimeMarshallingInst).unmarshaller(commonHttpSerializers.dateTimeHttpSerializer, executionContext, materializer)

  override def finiteDurationUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[scala.concurrent.duration.FiniteDuration] =
    ContentTypeBoundUnmarshallerFactory[scala.concurrent.duration.FiniteDuration](commonContentTypeProviders.finiteDurationTypeProvider, DefaultMarshallingInstances.DurationMarshallingInst).unmarshaller(commonHttpSerializers.finiteDurationHttpSerializer, executionContext, materializer)

  override def booleansUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Boolean]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Boolean]](commonContentTypeProviders.booleansContentTypeProvider, DefaultMarshallingInstances.BooleansMarshallingInst).unmarshaller(commonHttpSerializers.booleansHttpSerializer, executionContext, materializer)

  override def stringsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[String]] =
    ContentTypeBoundUnmarshallerFactory[Seq[String]](commonContentTypeProviders.stringsContentTypeProvider, DefaultMarshallingInstances.StringsMarshallingInst).unmarshaller(commonHttpSerializers.stringsHttpSerializer, executionContext, materializer)

  override def bytesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Byte]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Byte]](commonContentTypeProviders.bytesContentTypeProvider, DefaultMarshallingInstances.BytesMarshallingInst).unmarshaller(commonHttpSerializers.bytesHttpSerializer, executionContext, materializer)

  override def shortsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Short]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Short]](commonContentTypeProviders.shortsContentTypeProvider, DefaultMarshallingInstances.ShortsMarshallingInst).unmarshaller(commonHttpSerializers.shortsHttpSerializer, executionContext, materializer)

  override def intsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Int]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Int]](commonContentTypeProviders.intsContentTypeProvider, DefaultMarshallingInstances.IntsMarshallingInst).unmarshaller(commonHttpSerializers.intsHttpSerializer, executionContext, materializer)

  override def longsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Long]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Long]](commonContentTypeProviders.longsContentTypeProvider, DefaultMarshallingInstances.LongsMarshallingInst).unmarshaller(commonHttpSerializers.longsHttpSerializer, executionContext, materializer)

  override def bigIntsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[BigInt]] =
    ContentTypeBoundUnmarshallerFactory[Seq[BigInt]](commonContentTypeProviders.bigIntsContentTypeProvider, DefaultMarshallingInstances.BigIntsMarshallingInst).unmarshaller(commonHttpSerializers.bigIntsHttpSerializer, executionContext, materializer)

  override def floatsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Float]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Float]](commonContentTypeProviders.floatsContentTypeProvider, DefaultMarshallingInstances.FloatsMarshallingInst).unmarshaller(commonHttpSerializers.floatsHttpSerializer, executionContext, materializer)

  override def doublesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[Double]] =
    ContentTypeBoundUnmarshallerFactory[Seq[Double]](commonContentTypeProviders.doublesContentTypeProvider, DefaultMarshallingInstances.DoublesMarshallingInst).unmarshaller(commonHttpSerializers.doublesHttpSerializer, executionContext, materializer)

  override def bigDecimalsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[BigDecimal]] =
    ContentTypeBoundUnmarshallerFactory[Seq[BigDecimal]](commonContentTypeProviders.bigDecimalsContentTypeProvider, DefaultMarshallingInstances.BigDecimalsMarshallingInst).unmarshaller(commonHttpSerializers.bigDecimalsHttpSerializer, executionContext, materializer)

  override def urisUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.net.URI]] =
    ContentTypeBoundUnmarshallerFactory[Seq[java.net.URI]](commonContentTypeProviders.urisContentTypeProvider, DefaultMarshallingInstances.UrisMarshallingInst).unmarshaller(commonHttpSerializers.urisHttpSerializer, executionContext, materializer)

  override def uuidsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.util.UUID]] =
    ContentTypeBoundUnmarshallerFactory[Seq[java.util.UUID]](commonContentTypeProviders.uuidsContentTypeProvider, DefaultMarshallingInstances.UuidsMarshallingInst).unmarshaller(commonHttpSerializers.uuidsHttpSerializer, executionContext, materializer)

  override def localDateTimesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.time.LocalDateTime]] =
    ContentTypeBoundUnmarshallerFactory[Seq[java.time.LocalDateTime]](commonContentTypeProviders.localDateTimesContentTypeProvider, DefaultMarshallingInstances.LocalDateTimesMarshallingInst).unmarshaller(commonHttpSerializers.localDateTimesHttpSerializer, executionContext, materializer)

  override def dateTimesUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[java.time.ZonedDateTime]] =
    ContentTypeBoundUnmarshallerFactory[Seq[java.time.ZonedDateTime]](commonContentTypeProviders.dateTimesContentTypeProvider, DefaultMarshallingInstances.DateTimesMarshallingInst).unmarshaller(commonHttpSerializers.dateTimesHttpSerializer, executionContext, materializer)

  override def finiteDurationsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[scala.concurrent.duration.FiniteDuration]] =
    ContentTypeBoundUnmarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]](commonContentTypeProviders.finiteDurationsContentTypeProvider, DefaultMarshallingInstances.DurationsMarshallingInst).unmarshaller(commonHttpSerializers.finiteDurationsHttpSerializer, executionContext, materializer)

  override def eventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.Event] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Event](commonContentTypeProviders.eventContentTypeProvider, DefaultMarshallingInstances.EventMarshallingInst).unmarshaller(commonHttpSerializers.eventHttpSerializer, executionContext, materializer)

  override def systemEventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.SystemEvent] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.SystemEvent](commonContentTypeProviders.systemEventContentTypeProvider, DefaultMarshallingInstances.SystemEventMarshallingInst).unmarshaller(commonHttpSerializers.systemEventHttpSerializer, executionContext, materializer)

  override def domainEventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.DomainEvent] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.DomainEvent](commonContentTypeProviders.domainEventContentTypeProvider, DefaultMarshallingInstances.DomainEventMarshallingInst).unmarshaller(commonHttpSerializers.domainEventHttpSerializer, executionContext, materializer)

  override def aggregateRootEventUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.AggregateRootEvent] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.AggregateRootEvent](commonContentTypeProviders.aggregateRootEventContentTypeProvider, DefaultMarshallingInstances.AggregateRootEventMarshallingInst).unmarshaller(commonHttpSerializers.aggregateRootEventHttpSerializer, executionContext, materializer)

  override def commandUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.Command] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Command](commonContentTypeProviders.commandContentTypeProvider, DefaultMarshallingInstances.CommandMarshallingInst).unmarshaller(commonHttpSerializers.commandHttpSerializer, executionContext, materializer)

  override def problemUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.common.Problem] =
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Problem](commonContentTypeProviders.problemContentTypeProvider, DefaultMarshallingInstances.ProblemMarshallingInst).unmarshaller(commonHttpSerializers.problemHttpSerializer, executionContext, materializer)

  override def commandResponseUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[almhirt.tracking.CommandResponse] =
    ContentTypeBoundUnmarshallerFactory[almhirt.tracking.CommandResponse](commonContentTypeProviders.commandResponseContentTypeProvider, DefaultMarshallingInstances.CommandResponseMarshallingInst).unmarshaller(commonHttpSerializers.commandResponseHttpSerializer, executionContext, materializer)

  override def eventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.Event]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Event]](commonContentTypeProviders.eventsContentTypeProvider, DefaultMarshallingInstances.EventsMarshallingInst).unmarshaller(commonHttpSerializers.eventsHttpSerializer, executionContext, materializer)

  override def systemEventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.SystemEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.SystemEvent]](commonContentTypeProviders.systemEventsContentTypeProvider, DefaultMarshallingInstances.SystemEventsMarshallingInst ).unmarshaller(commonHttpSerializers.systemEventsHttpSerializer, executionContext, materializer)

  override def domainEventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.DomainEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.DomainEvent]](commonContentTypeProviders.domainEventsContentTypeProvider, DefaultMarshallingInstances.DomainEventsMarshallingInst ).unmarshaller(commonHttpSerializers.domainEventsHttpSerializer, executionContext, materializer)

  override def aggregateRootEventsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.AggregateRootEvent]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.AggregateRootEvent]](commonContentTypeProviders.aggregateRootEventsContentTypeProvider, DefaultMarshallingInstances.AggregateRootEventsMarshallingInst ).unmarshaller(commonHttpSerializers.aggregateRootEventsHttpSerializer, executionContext, materializer)

    
  override def commandsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.Command]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Command]](commonContentTypeProviders.commandsContentTypeProvider, DefaultMarshallingInstances.CommandsMarshallingInst).unmarshaller(commonHttpSerializers.commandsHttpSerializer, executionContext, materializer)

  override def problemsUnmarshaller(implicit executionContext: ExecutionContext, materializer: Materializer): FromEntityUnmarshaller[Seq[almhirt.common.Problem]] =
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Problem]](commonContentTypeProviders.problemsContentTypeProvider, DefaultMarshallingInstances.ProblemsMarshallingInst).unmarshaller(commonHttpSerializers.problemsHttpSerializer, executionContext, materializer)

}