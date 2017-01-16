package almhirt.httpx.akkahttp.marshalling

import almhirt.http.HasCommonHttpSerializers
import akka.http.scaladsl.marshalling._

trait HasCommonMarshallers extends HasProblemMarshaller {
  implicit def booleanMarshaller: ToEntityMarshaller[Boolean]
  implicit def stringMarshaller: ToEntityMarshaller[String]
  implicit def byteMarshaller: ToEntityMarshaller[Byte]
  implicit def shortMarshaller: ToEntityMarshaller[Short]
  implicit def intMarshaller: ToEntityMarshaller[Int]
  implicit def longMarshaller: ToEntityMarshaller[Long]
  implicit def bigIntMarshaller: ToEntityMarshaller[BigInt]
  implicit def floatMarshaller: ToEntityMarshaller[Float]
  implicit def doubleMarshaller: ToEntityMarshaller[Double]
  implicit def bigDecimalMarshaller: ToEntityMarshaller[BigDecimal]
  implicit def uriMarshaller: ToEntityMarshaller[java.net.URI]
  implicit def uuidMarshaller: ToEntityMarshaller[java.util.UUID]
  implicit def localDateTimeMarshaller: ToEntityMarshaller[java.time.LocalDateTime]
  implicit def dateTimeMarshaller: ToEntityMarshaller[java.time.ZonedDateTime]
  implicit def finiteDurationMarshaller: ToEntityMarshaller[scala.concurrent.duration.FiniteDuration]

  implicit def booleansMarshaller: ToEntityMarshaller[Seq[Boolean]]
  implicit def stringsMarshaller: ToEntityMarshaller[Seq[String]]
  implicit def bytesMarshaller: ToEntityMarshaller[Seq[Byte]]
  implicit def shortsMarshaller: ToEntityMarshaller[Seq[Short]]
  implicit def intsMarshaller: ToEntityMarshaller[Seq[Int]]
  implicit def longsMarshaller: ToEntityMarshaller[Seq[Long]]
  implicit def bigIntsMarshaller: ToEntityMarshaller[Seq[BigInt]]
  implicit def floatsMarshaller: ToEntityMarshaller[Seq[Float]]
  implicit def doublesMarshaller: ToEntityMarshaller[Seq[Double]]
  implicit def bigDecimalsMarshaller: ToEntityMarshaller[Seq[BigDecimal]]
  implicit def urisMarshaller: ToEntityMarshaller[Seq[java.net.URI]]
  implicit def uuidsMarshaller: ToEntityMarshaller[Seq[java.util.UUID]]
  implicit def localDateTimesMarshaller: ToEntityMarshaller[Seq[java.time.LocalDateTime]]
  implicit def dateTimesMarshaller: ToEntityMarshaller[Seq[java.time.ZonedDateTime]]
  implicit def finiteDurationsMarshaller: ToEntityMarshaller[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventMarshaller: ToEntityMarshaller[almhirt.common.Event]
  def systemEventMarshaller: ToEntityMarshaller[almhirt.common.SystemEvent]
  def domainEventMarshaller: ToEntityMarshaller[almhirt.common.DomainEvent]
  def aggregateRootEventMarshaller: ToEntityMarshaller[almhirt.common.AggregateRootEvent]
  implicit def commandMarshaller: ToEntityMarshaller[almhirt.common.Command]
  implicit def problemMarshaller: ToEntityMarshaller[almhirt.common.Problem]
  implicit def commandResponseMarshaller: ToEntityMarshaller[almhirt.tracking.CommandResponse]

  implicit def eventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.Event]]
  implicit def systemEventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.SystemEvent]]
  implicit def domainEventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.DomainEvent]]
  implicit def aggregateRootEventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.AggregateRootEvent]]
  implicit def commandsMarshaller: ToEntityMarshaller[Seq[almhirt.common.Command]]
  implicit def problemsMarshaller: ToEntityMarshaller[Seq[almhirt.common.Problem]]
}

trait CommonMarshallerInstances { self: HasCommonMarshallers ⇒
  def commonHttpSerializers: HasCommonHttpSerializers
  def commonContentTypeProviders: HasCommonContentTypeProviders

  override lazy val booleanMarshaller: ToEntityMarshaller[Boolean] =
    ContentTypeBoundMarshallerFactory[Boolean](commonContentTypeProviders.booleanContentTypeProvider, DefaultMarshallingInstances.BooleanMarshallingInst).marshaller(commonHttpSerializers.booleanHttpSerializer)

  override lazy val stringMarshaller: ToEntityMarshaller[String] =
    ContentTypeBoundMarshallerFactory[String](commonContentTypeProviders.stringContentTypeProvider, DefaultMarshallingInstances.StringMarshallingInst).marshaller(commonHttpSerializers.stringHttpSerializer)

  override lazy val byteMarshaller: ToEntityMarshaller[Byte] =
    ContentTypeBoundMarshallerFactory[Byte](commonContentTypeProviders.byteContentTypeProvider, DefaultMarshallingInstances.ByteMarshallingInst).marshaller(commonHttpSerializers.byteHttpSerializer)

  override lazy val shortMarshaller: ToEntityMarshaller[Short] =
    ContentTypeBoundMarshallerFactory[Short](commonContentTypeProviders.shortContentTypeProvider, DefaultMarshallingInstances.ShortMarshallingInst).marshaller(commonHttpSerializers.shortHttpSerializer)

  override lazy val intMarshaller: ToEntityMarshaller[Int] =
    ContentTypeBoundMarshallerFactory[Int](commonContentTypeProviders.intContentTypeProvider, DefaultMarshallingInstances.IntMarshallingInst).marshaller(commonHttpSerializers.intHttpSerializer)

  override lazy val longMarshaller: ToEntityMarshaller[Long] =
    ContentTypeBoundMarshallerFactory[Long](commonContentTypeProviders.longContentTypeProvider, DefaultMarshallingInstances.LongMarshallingInst).marshaller(commonHttpSerializers.longHttpSerializer)

  override lazy val bigIntMarshaller: ToEntityMarshaller[BigInt] =
    ContentTypeBoundMarshallerFactory[BigInt](commonContentTypeProviders.bigIntContentTypeProvider, DefaultMarshallingInstances.BigIntMarshallingInst).marshaller(commonHttpSerializers.bigIntHttpSerializer)

  override lazy val floatMarshaller: ToEntityMarshaller[Float] =
    ContentTypeBoundMarshallerFactory[Float](commonContentTypeProviders.floatContentTypeProvider, DefaultMarshallingInstances.FloatMarshallingInst).marshaller(commonHttpSerializers.floatHttpSerializer)

  override lazy val doubleMarshaller: ToEntityMarshaller[Double] =
    ContentTypeBoundMarshallerFactory[Double](commonContentTypeProviders.doubleContentTypeProvider, DefaultMarshallingInstances.DoubleMarshallingInst).marshaller(commonHttpSerializers.doubleHttpSerializer)

  override lazy val bigDecimalMarshaller: ToEntityMarshaller[BigDecimal] =
    ContentTypeBoundMarshallerFactory[BigDecimal](commonContentTypeProviders.bigDecimalContentTypeProvider, DefaultMarshallingInstances.BigDecimalMarshallingInst).marshaller(commonHttpSerializers.bigDecimalHttpSerializer)

  override lazy val uriMarshaller: ToEntityMarshaller[java.net.URI] =
    ContentTypeBoundMarshallerFactory[java.net.URI](commonContentTypeProviders.uriContentTypeProvider, DefaultMarshallingInstances.UriMarshallingInst).marshaller(commonHttpSerializers.uriHttpSerializer)

  override lazy val uuidMarshaller: ToEntityMarshaller[java.util.UUID] =
    ContentTypeBoundMarshallerFactory[java.util.UUID](commonContentTypeProviders.uuidContentTypeProvider, DefaultMarshallingInstances.UuidMarshallingInst).marshaller(commonHttpSerializers.uuidHttpSerializer)

  override lazy val localDateTimeMarshaller: ToEntityMarshaller[java.time.LocalDateTime] =
    ContentTypeBoundMarshallerFactory[java.time.LocalDateTime](commonContentTypeProviders.localDateTimeContentTypeProvider, DefaultMarshallingInstances.LocalDateTimeMarshallingInst).marshaller(commonHttpSerializers.localDateTimeHttpSerializer)

  override lazy val dateTimeMarshaller: ToEntityMarshaller[java.time.ZonedDateTime] =
    ContentTypeBoundMarshallerFactory[java.time.ZonedDateTime](commonContentTypeProviders.dateTimeContentTypeProvider, DefaultMarshallingInstances.DateTimeMarshallingInst).marshaller(commonHttpSerializers.dateTimeHttpSerializer)

  override lazy val finiteDurationMarshaller: ToEntityMarshaller[scala.concurrent.duration.FiniteDuration] =
    ContentTypeBoundMarshallerFactory[scala.concurrent.duration.FiniteDuration](commonContentTypeProviders.finiteDurationTypeProvider, DefaultMarshallingInstances.DurationMarshallingInst).marshaller(commonHttpSerializers.finiteDurationHttpSerializer)

  override lazy val booleansMarshaller: ToEntityMarshaller[Seq[Boolean]] =
    ContentTypeBoundMarshallerFactory[Seq[Boolean]](commonContentTypeProviders.booleansContentTypeProvider, DefaultMarshallingInstances.BooleansMarshallingInst).marshaller(commonHttpSerializers.booleansHttpSerializer)

  override lazy val stringsMarshaller: ToEntityMarshaller[Seq[String]] =
    ContentTypeBoundMarshallerFactory[Seq[String]](commonContentTypeProviders.stringsContentTypeProvider, DefaultMarshallingInstances.StringsMarshallingInst).marshaller(commonHttpSerializers.stringsHttpSerializer)

  override lazy val bytesMarshaller: ToEntityMarshaller[Seq[Byte]] =
    ContentTypeBoundMarshallerFactory[Seq[Byte]](commonContentTypeProviders.bytesContentTypeProvider, DefaultMarshallingInstances.BytesMarshallingInst).marshaller(commonHttpSerializers.bytesHttpSerializer)

  override lazy val shortsMarshaller: ToEntityMarshaller[Seq[Short]] =
    ContentTypeBoundMarshallerFactory[Seq[Short]](commonContentTypeProviders.shortsContentTypeProvider, DefaultMarshallingInstances.ShortsMarshallingInst).marshaller(commonHttpSerializers.shortsHttpSerializer)

  override lazy val intsMarshaller: ToEntityMarshaller[Seq[Int]] =
    ContentTypeBoundMarshallerFactory[Seq[Int]](commonContentTypeProviders.intsContentTypeProvider, DefaultMarshallingInstances.IntsMarshallingInst).marshaller(commonHttpSerializers.intsHttpSerializer)

  override lazy val longsMarshaller: ToEntityMarshaller[Seq[Long]] =
    ContentTypeBoundMarshallerFactory[Seq[Long]](commonContentTypeProviders.longsContentTypeProvider, DefaultMarshallingInstances.LongsMarshallingInst).marshaller(commonHttpSerializers.longsHttpSerializer)

  override lazy val bigIntsMarshaller: ToEntityMarshaller[Seq[BigInt]] =
    ContentTypeBoundMarshallerFactory[Seq[BigInt]](commonContentTypeProviders.bigIntsContentTypeProvider, DefaultMarshallingInstances.BigIntsMarshallingInst).marshaller(commonHttpSerializers.bigIntsHttpSerializer)

  override lazy val floatsMarshaller: ToEntityMarshaller[Seq[Float]] =
    ContentTypeBoundMarshallerFactory[Seq[Float]](commonContentTypeProviders.floatsContentTypeProvider, DefaultMarshallingInstances.FloatsMarshallingInst).marshaller(commonHttpSerializers.floatsHttpSerializer)

  override lazy val doublesMarshaller: ToEntityMarshaller[Seq[Double]] =
    ContentTypeBoundMarshallerFactory[Seq[Double]](commonContentTypeProviders.doublesContentTypeProvider, DefaultMarshallingInstances.DoublesMarshallingInst).marshaller(commonHttpSerializers.doublesHttpSerializer)

  override lazy val bigDecimalsMarshaller: ToEntityMarshaller[Seq[BigDecimal]] =
    ContentTypeBoundMarshallerFactory[Seq[BigDecimal]](commonContentTypeProviders.bigDecimalsContentTypeProvider, DefaultMarshallingInstances.BigDecimalsMarshallingInst).marshaller(commonHttpSerializers.bigDecimalsHttpSerializer)

  override lazy val urisMarshaller: ToEntityMarshaller[Seq[java.net.URI]] =
    ContentTypeBoundMarshallerFactory[Seq[java.net.URI]](commonContentTypeProviders.urisContentTypeProvider, DefaultMarshallingInstances.UrisMarshallingInst).marshaller(commonHttpSerializers.urisHttpSerializer)

  override lazy val uuidsMarshaller: ToEntityMarshaller[Seq[java.util.UUID]] =
    ContentTypeBoundMarshallerFactory[Seq[java.util.UUID]](commonContentTypeProviders.uuidsContentTypeProvider, DefaultMarshallingInstances.UuidsMarshallingInst).marshaller(commonHttpSerializers.uuidsHttpSerializer)

  override lazy val localDateTimesMarshaller: ToEntityMarshaller[Seq[java.time.LocalDateTime]] =
    ContentTypeBoundMarshallerFactory[Seq[java.time.LocalDateTime]](commonContentTypeProviders.localDateTimesContentTypeProvider, DefaultMarshallingInstances.LocalDateTimesMarshallingInst).marshaller(commonHttpSerializers.localDateTimesHttpSerializer)

  override lazy val dateTimesMarshaller: ToEntityMarshaller[Seq[java.time.ZonedDateTime]] =
    ContentTypeBoundMarshallerFactory[Seq[java.time.ZonedDateTime]](commonContentTypeProviders.dateTimesContentTypeProvider, DefaultMarshallingInstances.DateTimesMarshallingInst).marshaller(commonHttpSerializers.dateTimesHttpSerializer)

  override lazy val finiteDurationsMarshaller: ToEntityMarshaller[Seq[scala.concurrent.duration.FiniteDuration]] =
    ContentTypeBoundMarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]](commonContentTypeProviders.finiteDurationsContentTypeProvider, DefaultMarshallingInstances.DurationsMarshallingInst).marshaller(commonHttpSerializers.finiteDurationsHttpSerializer)

  override lazy val eventMarshaller: ToEntityMarshaller[almhirt.common.Event] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Event](commonContentTypeProviders.eventContentTypeProvider, DefaultMarshallingInstances.EventMarshallingInst).marshaller(commonHttpSerializers.eventHttpSerializer)

  override lazy val systemEventMarshaller: ToEntityMarshaller[almhirt.common.SystemEvent] =
    ContentTypeBoundMarshallerFactory[almhirt.common.SystemEvent](commonContentTypeProviders.systemEventContentTypeProvider, DefaultMarshallingInstances.SystemEventMarshallingInst).marshaller(commonHttpSerializers.systemEventHttpSerializer)

  override lazy val domainEventMarshaller: ToEntityMarshaller[almhirt.common.DomainEvent] =
    ContentTypeBoundMarshallerFactory[almhirt.common.DomainEvent](commonContentTypeProviders.domainEventContentTypeProvider, DefaultMarshallingInstances.DomainEventMarshallingInst ).marshaller(commonHttpSerializers.domainEventHttpSerializer)

  override lazy val aggregateRootEventMarshaller: ToEntityMarshaller[almhirt.common.AggregateRootEvent] =
    ContentTypeBoundMarshallerFactory[almhirt.common.AggregateRootEvent](commonContentTypeProviders.aggregateRootEventContentTypeProvider, DefaultMarshallingInstances.AggregateRootEventMarshallingInst).marshaller(commonHttpSerializers.aggregateRootEventHttpSerializer)

  override lazy val commandMarshaller: ToEntityMarshaller[almhirt.common.Command] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Command](commonContentTypeProviders.commandContentTypeProvider, DefaultMarshallingInstances.CommandMarshallingInst).marshaller(commonHttpSerializers.commandHttpSerializer)

  override lazy val problemMarshaller: ToEntityMarshaller[almhirt.common.Problem] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Problem](commonContentTypeProviders.problemContentTypeProvider, DefaultMarshallingInstances.ProblemMarshallingInst).marshaller(commonHttpSerializers.problemHttpSerializer)

  override lazy val commandResponseMarshaller: ToEntityMarshaller[almhirt.tracking.CommandResponse] =
    ContentTypeBoundMarshallerFactory[almhirt.tracking.CommandResponse](commonContentTypeProviders.commandResponseContentTypeProvider, DefaultMarshallingInstances.CommandResponseMarshallingInst).marshaller(commonHttpSerializers.commandResponseHttpSerializer)

  override lazy val eventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.Event]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Event]](commonContentTypeProviders.eventsContentTypeProvider, DefaultMarshallingInstances.EventsMarshallingInst).marshaller(commonHttpSerializers.eventsHttpSerializer)

  override lazy val systemEventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.SystemEvent]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.SystemEvent]](commonContentTypeProviders.systemEventsContentTypeProvider, DefaultMarshallingInstances.SystemEventsMarshallingInst ).marshaller(commonHttpSerializers.systemEventsHttpSerializer)

  override lazy val domainEventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.DomainEvent]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.DomainEvent]](commonContentTypeProviders.domainEventsContentTypeProvider, DefaultMarshallingInstances.DomainEventsMarshallingInst ).marshaller(commonHttpSerializers.domainEventsHttpSerializer)

  override lazy val aggregateRootEventsMarshaller: ToEntityMarshaller[Seq[almhirt.common.AggregateRootEvent]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.AggregateRootEvent]](commonContentTypeProviders.aggregateRootEventsContentTypeProvider, DefaultMarshallingInstances.AggregateRootEventsMarshallingInst ).marshaller(commonHttpSerializers.aggregateRootEventsHttpSerializer)

  override lazy val commandsMarshaller: ToEntityMarshaller[Seq[almhirt.common.Command]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Command]](commonContentTypeProviders.commandsContentTypeProvider, DefaultMarshallingInstances.CommandsMarshallingInst).marshaller(commonHttpSerializers.commandsHttpSerializer)

  override lazy val problemsMarshaller: ToEntityMarshaller[Seq[almhirt.common.Problem]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Problem]](commonContentTypeProviders.problemsContentTypeProvider, DefaultMarshallingInstances.ProblemsMarshallingInst).marshaller(commonHttpSerializers.problemsHttpSerializer)

    
}