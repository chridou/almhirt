package almhirt.httpx.spray.marshalling

import almhirt.http.HasCommonHttpSerializers
import spray.httpx.marshalling.Marshaller

trait HasCommonMarshallers extends HasProblemMarshaller {
  implicit def booleanMarshaller: Marshaller[Boolean]
  implicit def stringMarshaller: Marshaller[String]
  implicit def byteMarshaller: Marshaller[Byte]
  implicit def shortMarshaller: Marshaller[Short]
  implicit def intMarshaller: Marshaller[Int]
  implicit def longMarshaller: Marshaller[Long]
  implicit def bigIntMarshaller: Marshaller[BigInt]
  implicit def floatMarshaller: Marshaller[Float]
  implicit def doubleMarshaller: Marshaller[Double]
  implicit def bigDecimalMarshaller: Marshaller[BigDecimal]
  implicit def uriMarshaller: Marshaller[java.net.URI]
  implicit def uuidMarshaller: Marshaller[java.util.UUID]
  implicit def localDateTimeMarshaller: Marshaller[org.joda.time.LocalDateTime]
  implicit def dateTimeMarshaller: Marshaller[org.joda.time.DateTime]
  implicit def finiteDurationMarshaller: Marshaller[scala.concurrent.duration.FiniteDuration]

  implicit def booleansMarshaller: Marshaller[Seq[Boolean]]
  implicit def stringsMarshaller: Marshaller[Seq[String]]
  implicit def bytesMarshaller: Marshaller[Seq[Byte]]
  implicit def shortsMarshaller: Marshaller[Seq[Short]]
  implicit def intsMarshaller: Marshaller[Seq[Int]]
  implicit def longsMarshaller: Marshaller[Seq[Long]]
  implicit def bigIntsMarshaller: Marshaller[Seq[BigInt]]
  implicit def floatsMarshaller: Marshaller[Seq[Float]]
  implicit def doublesMarshaller: Marshaller[Seq[Double]]
  implicit def bigDecimalsMarshaller: Marshaller[Seq[BigDecimal]]
  implicit def urisMarshaller: Marshaller[Seq[java.net.URI]]
  implicit def uuidsMarshaller: Marshaller[Seq[java.util.UUID]]
  implicit def localDateTimesMarshaller: Marshaller[Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesMarshaller: Marshaller[Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsMarshaller: Marshaller[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventMarshaller: Marshaller[almhirt.common.Event]
  implicit def commandMarshaller: Marshaller[almhirt.common.Command]
  implicit def problemMarshaller: Marshaller[almhirt.common.Problem]

  implicit def eventsMarshaller: Marshaller[Seq[almhirt.common.Event]]
  implicit def commandsMarshaller: Marshaller[Seq[almhirt.common.Command]]
  implicit def problemsMarshaller: Marshaller[Seq[almhirt.common.Problem]]
}

trait CommonMarshallerInstances { self: HasCommonMarshallers =>
  def commonHttpSerializers: HasCommonHttpSerializers
  def commonContentTypeProviders: HasCommonContentTypeProviders

  override lazy val booleanMarshaller: Marshaller[Boolean] =
    ContentTypeBoundMarshallerFactory[Boolean](commonContentTypeProviders.booleanContentTypeProvider, DefaultMarshallingInstances.BooleanMarshallingInst).marshaller(commonHttpSerializers.booleanHttpSerializer)

  override lazy val stringMarshaller: Marshaller[String] =
    ContentTypeBoundMarshallerFactory[String](commonContentTypeProviders.stringContentTypeProvider, DefaultMarshallingInstances.StringMarshallingInst).marshaller(commonHttpSerializers.stringHttpSerializer)

  override lazy val byteMarshaller: Marshaller[Byte] =
    ContentTypeBoundMarshallerFactory[Byte](commonContentTypeProviders.byteContentTypeProvider, DefaultMarshallingInstances.ByteMarshallingInst).marshaller(commonHttpSerializers.byteHttpSerializer)

  override lazy val shortMarshaller: Marshaller[Short] =
    ContentTypeBoundMarshallerFactory[Short](commonContentTypeProviders.shortContentTypeProvider, DefaultMarshallingInstances.ShortMarshallingInst).marshaller(commonHttpSerializers.shortHttpSerializer)

  override lazy val intMarshaller: Marshaller[Int] =
    ContentTypeBoundMarshallerFactory[Int](commonContentTypeProviders.intContentTypeProvider, DefaultMarshallingInstances.IntMarshallingInst).marshaller(commonHttpSerializers.intHttpSerializer)

  override lazy val longMarshaller: Marshaller[Long] =
    ContentTypeBoundMarshallerFactory[Long](commonContentTypeProviders.longContentTypeProvider, DefaultMarshallingInstances.LongMarshallingInst).marshaller(commonHttpSerializers.longHttpSerializer)

  override lazy val bigIntMarshaller: Marshaller[BigInt] =
    ContentTypeBoundMarshallerFactory[BigInt](commonContentTypeProviders.bigIntContentTypeProvider, DefaultMarshallingInstances.BigIntMarshallingInst).marshaller(commonHttpSerializers.bigIntHttpSerializer)

  override lazy val floatMarshaller: Marshaller[Float] =
    ContentTypeBoundMarshallerFactory[Float](commonContentTypeProviders.floatContentTypeProvider, DefaultMarshallingInstances.FloatMarshallingInst).marshaller(commonHttpSerializers.floatHttpSerializer)

  override lazy val doubleMarshaller: Marshaller[Double] =
    ContentTypeBoundMarshallerFactory[Double](commonContentTypeProviders.doubleContentTypeProvider, DefaultMarshallingInstances.DoubleMarshallingInst).marshaller(commonHttpSerializers.doubleHttpSerializer)

  override lazy val bigDecimalMarshaller: Marshaller[BigDecimal] =
    ContentTypeBoundMarshallerFactory[BigDecimal](commonContentTypeProviders.bigDecimalContentTypeProvider, DefaultMarshallingInstances.BigDecimalMarshallingInst).marshaller(commonHttpSerializers.bigDecimalHttpSerializer)

  override lazy val uriMarshaller: Marshaller[java.net.URI] =
    ContentTypeBoundMarshallerFactory[java.net.URI](commonContentTypeProviders.uriContentTypeProvider, DefaultMarshallingInstances.UriMarshallingInst).marshaller(commonHttpSerializers.uriHttpSerializer)

  override lazy val uuidMarshaller: Marshaller[java.util.UUID] =
    ContentTypeBoundMarshallerFactory[java.util.UUID](commonContentTypeProviders.uuidContentTypeProvider, DefaultMarshallingInstances.UuidMarshallingInst).marshaller(commonHttpSerializers.uuidHttpSerializer)

  override lazy val localDateTimeMarshaller: Marshaller[org.joda.time.LocalDateTime] =
    ContentTypeBoundMarshallerFactory[org.joda.time.LocalDateTime](commonContentTypeProviders.localDateTimeContentTypeProvider, DefaultMarshallingInstances.LocalDateTimeMarshallingInst).marshaller(commonHttpSerializers.localDateTimeHttpSerializer)

  override lazy val dateTimeMarshaller: Marshaller[org.joda.time.DateTime] =
    ContentTypeBoundMarshallerFactory[org.joda.time.DateTime](commonContentTypeProviders.dateTimeContentTypeProvider, DefaultMarshallingInstances.DateTimeMarshallingInst).marshaller(commonHttpSerializers.dateTimeHttpSerializer)

  override lazy val finiteDurationMarshaller: Marshaller[scala.concurrent.duration.FiniteDuration] =
    ContentTypeBoundMarshallerFactory[scala.concurrent.duration.FiniteDuration](commonContentTypeProviders.finiteDurationTypeProvider, DefaultMarshallingInstances.DurationMarshallingInst).marshaller(commonHttpSerializers.finiteDurationHttpSerializer)

  override lazy val booleansMarshaller: Marshaller[Seq[Boolean]] =
    ContentTypeBoundMarshallerFactory[Seq[Boolean]](commonContentTypeProviders.booleansContentTypeProvider, DefaultMarshallingInstances.BooleansMarshallingInst).marshaller(commonHttpSerializers.booleansHttpSerializer)

  override lazy val stringsMarshaller: Marshaller[Seq[String]] =
    ContentTypeBoundMarshallerFactory[Seq[String]](commonContentTypeProviders.stringsContentTypeProvider, DefaultMarshallingInstances.StringsMarshallingInst).marshaller(commonHttpSerializers.stringsHttpSerializer)

  override lazy val bytesMarshaller: Marshaller[Seq[Byte]] =
    ContentTypeBoundMarshallerFactory[Seq[Byte]](commonContentTypeProviders.bytesContentTypeProvider, DefaultMarshallingInstances.BytesMarshallingInst).marshaller(commonHttpSerializers.bytesHttpSerializer)

  override lazy val shortsMarshaller: Marshaller[Seq[Short]] =
    ContentTypeBoundMarshallerFactory[Seq[Short]](commonContentTypeProviders.shortsContentTypeProvider, DefaultMarshallingInstances.ShortsMarshallingInst).marshaller(commonHttpSerializers.shortsHttpSerializer)

  override lazy val intsMarshaller: Marshaller[Seq[Int]] =
    ContentTypeBoundMarshallerFactory[Seq[Int]](commonContentTypeProviders.intsContentTypeProvider, DefaultMarshallingInstances.IntsMarshallingInst).marshaller(commonHttpSerializers.intsHttpSerializer)

  override lazy val longsMarshaller: Marshaller[Seq[Long]] =
    ContentTypeBoundMarshallerFactory[Seq[Long]](commonContentTypeProviders.longsContentTypeProvider, DefaultMarshallingInstances.LongsMarshallingInst).marshaller(commonHttpSerializers.longsHttpSerializer)

  override lazy val bigIntsMarshaller: Marshaller[Seq[BigInt]] =
    ContentTypeBoundMarshallerFactory[Seq[BigInt]](commonContentTypeProviders.bigIntsContentTypeProvider, DefaultMarshallingInstances.BigIntsMarshallingInst).marshaller(commonHttpSerializers.bigIntsHttpSerializer)

  override lazy val floatsMarshaller: Marshaller[Seq[Float]] =
    ContentTypeBoundMarshallerFactory[Seq[Float]](commonContentTypeProviders.floatsContentTypeProvider, DefaultMarshallingInstances.FloatsMarshallingInst).marshaller(commonHttpSerializers.floatsHttpSerializer)

  override lazy val doublesMarshaller: Marshaller[Seq[Double]] =
    ContentTypeBoundMarshallerFactory[Seq[Double]](commonContentTypeProviders.doublesContentTypeProvider, DefaultMarshallingInstances.DoublesMarshallingInst).marshaller(commonHttpSerializers.doublesHttpSerializer)

  override lazy val bigDecimalsMarshaller: Marshaller[Seq[BigDecimal]] =
    ContentTypeBoundMarshallerFactory[Seq[BigDecimal]](commonContentTypeProviders.bigDecimalsContentTypeProvider, DefaultMarshallingInstances.BigDecimalsMarshallingInst).marshaller(commonHttpSerializers.bigDecimalsHttpSerializer)

  override lazy val urisMarshaller: Marshaller[Seq[java.net.URI]] =
    ContentTypeBoundMarshallerFactory[Seq[java.net.URI]](commonContentTypeProviders.urisContentTypeProvider, DefaultMarshallingInstances.UrisMarshallingInst).marshaller(commonHttpSerializers.urisHttpSerializer)

  override lazy val uuidsMarshaller: Marshaller[Seq[java.util.UUID]] =
    ContentTypeBoundMarshallerFactory[Seq[java.util.UUID]](commonContentTypeProviders.uuidsContentTypeProvider, DefaultMarshallingInstances.UuidsMarshallingInst).marshaller(commonHttpSerializers.uuidsHttpSerializer)

  override lazy val localDateTimesMarshaller: Marshaller[Seq[org.joda.time.LocalDateTime]] =
    ContentTypeBoundMarshallerFactory[Seq[org.joda.time.LocalDateTime]](commonContentTypeProviders.localDateTimesContentTypeProvider, DefaultMarshallingInstances.LocalDateTimesMarshallingInst).marshaller(commonHttpSerializers.localDateTimesHttpSerializer)

  override lazy val dateTimesMarshaller: Marshaller[Seq[org.joda.time.DateTime]] =
    ContentTypeBoundMarshallerFactory[Seq[org.joda.time.DateTime]](commonContentTypeProviders.dateTimesContentTypeProvider, DefaultMarshallingInstances.DateTimesMarshallingInst).marshaller(commonHttpSerializers.dateTimesHttpSerializer)

  override lazy val finiteDurationsMarshaller: Marshaller[Seq[scala.concurrent.duration.FiniteDuration]] =
    ContentTypeBoundMarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]](commonContentTypeProviders.finiteDurationsContentTypeProvider, DefaultMarshallingInstances.DurationsMarshallingInst).marshaller(commonHttpSerializers.finiteDurationsHttpSerializer)

  override lazy val eventMarshaller: Marshaller[almhirt.common.Event] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Event](commonContentTypeProviders.eventContentTypeProvider, DefaultMarshallingInstances.EventMarshallingInst).marshaller(commonHttpSerializers.eventHttpSerializer)

  override lazy val commandMarshaller: Marshaller[almhirt.common.Command] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Command](commonContentTypeProviders.commandContentTypeProvider, DefaultMarshallingInstances.CommandMarshallingInst).marshaller(commonHttpSerializers.commandHttpSerializer)

  override lazy val problemMarshaller: Marshaller[almhirt.common.Problem] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Problem](commonContentTypeProviders.problemContentTypeProvider, DefaultMarshallingInstances.ProblemMarshallingInst).marshaller(commonHttpSerializers.problemHttpSerializer)

  override lazy val eventsMarshaller: Marshaller[Seq[almhirt.common.Event]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Event]](commonContentTypeProviders.eventsContentTypeProvider, DefaultMarshallingInstances.EventsMarshallingInst).marshaller(commonHttpSerializers.eventsHttpSerializer)

  override lazy val commandsMarshaller: Marshaller[Seq[almhirt.common.Command]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Command]](commonContentTypeProviders.commandsContentTypeProvider, DefaultMarshallingInstances.CommandsMarshallingInst).marshaller(commonHttpSerializers.commandsHttpSerializer)

  override lazy val problemsMarshaller: Marshaller[Seq[almhirt.common.Problem]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Problem]](commonContentTypeProviders.problemsContentTypeProvider, DefaultMarshallingInstances.ProblemsMarshallingInst).marshaller(commonHttpSerializers.problemsHttpSerializer)

}