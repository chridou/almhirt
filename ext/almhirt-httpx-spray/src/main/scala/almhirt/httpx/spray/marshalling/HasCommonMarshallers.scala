package almhirt.httpx.spray.marshalling

import almhirt.serialization.HasCommonWireSerializers
import spray.httpx.marshalling.Marshaller

trait HasCommonMarshallers {
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
  def commonWireSerializers: HasCommonWireSerializers
  def commonContentTypeProviders: HasCommonContentTypeProviders

  override lazy val booleanMarshaller: Marshaller[Boolean] =
    ContentTypeBoundMarshallerFactory[Boolean](commonContentTypeProviders.booleanContentTypeProvider, DefaultMarshallingInstances.BooleanMarshallingInst).marshaller(commonWireSerializers.booleanWireSerializer)

  override lazy val stringMarshaller: Marshaller[String] =
    ContentTypeBoundMarshallerFactory[String](commonContentTypeProviders.stringContentTypeProvider, DefaultMarshallingInstances.StringMarshallingInst).marshaller(commonWireSerializers.stringWireSerializer)

  override lazy val byteMarshaller: Marshaller[Byte] =
    ContentTypeBoundMarshallerFactory[Byte](commonContentTypeProviders.byteContentTypeProvider, DefaultMarshallingInstances.ByteMarshallingInst).marshaller(commonWireSerializers.byteWireSerializer)

  override lazy val shortMarshaller: Marshaller[Short] =
    ContentTypeBoundMarshallerFactory[Short](commonContentTypeProviders.shortContentTypeProvider, DefaultMarshallingInstances.ShortMarshallingInst).marshaller(commonWireSerializers.shortWireSerializer)

  override lazy val intMarshaller: Marshaller[Int] =
    ContentTypeBoundMarshallerFactory[Int](commonContentTypeProviders.intContentTypeProvider, DefaultMarshallingInstances.IntMarshallingInst).marshaller(commonWireSerializers.intWireSerializer)

  override lazy val longMarshaller: Marshaller[Long] =
    ContentTypeBoundMarshallerFactory[Long](commonContentTypeProviders.longContentTypeProvider, DefaultMarshallingInstances.LongMarshallingInst).marshaller(commonWireSerializers.longWireSerializer)

  override lazy val bigIntMarshaller: Marshaller[BigInt] =
    ContentTypeBoundMarshallerFactory[BigInt](commonContentTypeProviders.bigIntContentTypeProvider, DefaultMarshallingInstances.BigIntMarshallingInst).marshaller(commonWireSerializers.bigIntWireSerializer)

  override lazy val floatMarshaller: Marshaller[Float] =
    ContentTypeBoundMarshallerFactory[Float](commonContentTypeProviders.floatContentTypeProvider, DefaultMarshallingInstances.FloatMarshallingInst).marshaller(commonWireSerializers.floatWireSerializer)

  override lazy val doubleMarshaller: Marshaller[Double] =
    ContentTypeBoundMarshallerFactory[Double](commonContentTypeProviders.doubleContentTypeProvider, DefaultMarshallingInstances.DoubleMarshallingInst).marshaller(commonWireSerializers.doubleWireSerializer)

  override lazy val bigDecimalMarshaller: Marshaller[BigDecimal] =
    ContentTypeBoundMarshallerFactory[BigDecimal](commonContentTypeProviders.bigDecimalContentTypeProvider, DefaultMarshallingInstances.BigDecimalMarshallingInst).marshaller(commonWireSerializers.bigDecimalWireSerializer)

  override lazy val uriMarshaller: Marshaller[java.net.URI] =
    ContentTypeBoundMarshallerFactory[java.net.URI](commonContentTypeProviders.uriContentTypeProvider, DefaultMarshallingInstances.UriMarshallingInst).marshaller(commonWireSerializers.uriWireSerializer)

  override lazy val uuidMarshaller: Marshaller[java.util.UUID] =
    ContentTypeBoundMarshallerFactory[java.util.UUID](commonContentTypeProviders.uuidContentTypeProvider, DefaultMarshallingInstances.UuidMarshallingInst).marshaller(commonWireSerializers.uuidWireSerializer)

  override lazy val localDateTimeMarshaller: Marshaller[org.joda.time.LocalDateTime] =
    ContentTypeBoundMarshallerFactory[org.joda.time.LocalDateTime](commonContentTypeProviders.localDateTimeContentTypeProvider, DefaultMarshallingInstances.LocalDateTimeMarshallingInst).marshaller(commonWireSerializers.localDateTimeWireSerializer)

  override lazy val dateTimeMarshaller: Marshaller[org.joda.time.DateTime] =
    ContentTypeBoundMarshallerFactory[org.joda.time.DateTime](commonContentTypeProviders.dateTimeContentTypeProvider, DefaultMarshallingInstances.DateTimeMarshallingInst).marshaller(commonWireSerializers.dateTimeWireSerializer)

  override lazy val finiteDurationMarshaller: Marshaller[scala.concurrent.duration.FiniteDuration] =
    ContentTypeBoundMarshallerFactory[scala.concurrent.duration.FiniteDuration](commonContentTypeProviders.finiteDurationTypeProvider, DefaultMarshallingInstances.DurationMarshallingInst).marshaller(commonWireSerializers.finiteDurationWireSerializer)

  override lazy val booleansMarshaller: Marshaller[Seq[Boolean]] =
    ContentTypeBoundMarshallerFactory[Seq[Boolean]](commonContentTypeProviders.booleansContentTypeProvider, DefaultMarshallingInstances.BooleansMarshallingInst).marshaller(commonWireSerializers.booleansWireSerializer)

  override lazy val stringsMarshaller: Marshaller[Seq[String]] =
    ContentTypeBoundMarshallerFactory[Seq[String]](commonContentTypeProviders.stringsContentTypeProvider, DefaultMarshallingInstances.StringsMarshallingInst).marshaller(commonWireSerializers.stringsWireSerializer)

  override lazy val bytesMarshaller: Marshaller[Seq[Byte]] =
    ContentTypeBoundMarshallerFactory[Seq[Byte]](commonContentTypeProviders.bytesContentTypeProvider, DefaultMarshallingInstances.BytesMarshallingInst).marshaller(commonWireSerializers.bytesWireSerializer)

  override lazy val shortsMarshaller: Marshaller[Seq[Short]] =
    ContentTypeBoundMarshallerFactory[Seq[Short]](commonContentTypeProviders.shortsContentTypeProvider, DefaultMarshallingInstances.ShortsMarshallingInst).marshaller(commonWireSerializers.shortsWireSerializer)

  override lazy val intsMarshaller: Marshaller[Seq[Int]] =
    ContentTypeBoundMarshallerFactory[Seq[Int]](commonContentTypeProviders.intsContentTypeProvider, DefaultMarshallingInstances.IntsMarshallingInst).marshaller(commonWireSerializers.intsWireSerializer)

  override lazy val longsMarshaller: Marshaller[Seq[Long]] =
    ContentTypeBoundMarshallerFactory[Seq[Long]](commonContentTypeProviders.longsContentTypeProvider, DefaultMarshallingInstances.LongsMarshallingInst).marshaller(commonWireSerializers.longsWireSerializer)

  override lazy val bigIntsMarshaller: Marshaller[Seq[BigInt]] =
    ContentTypeBoundMarshallerFactory[Seq[BigInt]](commonContentTypeProviders.bigIntsContentTypeProvider, DefaultMarshallingInstances.BigIntsMarshallingInst).marshaller(commonWireSerializers.bigIntsWireSerializer)

  override lazy val floatsMarshaller: Marshaller[Seq[Float]] =
    ContentTypeBoundMarshallerFactory[Seq[Float]](commonContentTypeProviders.floatsContentTypeProvider, DefaultMarshallingInstances.FloatsMarshallingInst).marshaller(commonWireSerializers.floatsWireSerializer)

  override lazy val doublesMarshaller: Marshaller[Seq[Double]] =
    ContentTypeBoundMarshallerFactory[Seq[Double]](commonContentTypeProviders.doublesContentTypeProvider, DefaultMarshallingInstances.DoublesMarshallingInst).marshaller(commonWireSerializers.doublesWireSerializer)

  override lazy val bigDecimalsMarshaller: Marshaller[Seq[BigDecimal]] =
    ContentTypeBoundMarshallerFactory[Seq[BigDecimal]](commonContentTypeProviders.bigDecimalsContentTypeProvider, DefaultMarshallingInstances.BigDecimalsMarshallingInst).marshaller(commonWireSerializers.bigDecimalsWireSerializer)

  override lazy val urisMarshaller: Marshaller[Seq[java.net.URI]] =
    ContentTypeBoundMarshallerFactory[Seq[java.net.URI]](commonContentTypeProviders.urisContentTypeProvider, DefaultMarshallingInstances.UrisMarshallingInst).marshaller(commonWireSerializers.urisWireSerializer)

  override lazy val uuidsMarshaller: Marshaller[Seq[java.util.UUID]] =
    ContentTypeBoundMarshallerFactory[Seq[java.util.UUID]](commonContentTypeProviders.uuidsContentTypeProvider, DefaultMarshallingInstances.UuidsMarshallingInst).marshaller(commonWireSerializers.uuidsWireSerializer)

  override lazy val localDateTimesMarshaller: Marshaller[Seq[org.joda.time.LocalDateTime]] =
    ContentTypeBoundMarshallerFactory[Seq[org.joda.time.LocalDateTime]](commonContentTypeProviders.localDateTimesContentTypeProvider, DefaultMarshallingInstances.LocalDateTimesMarshallingInst).marshaller(commonWireSerializers.localDateTimesWireSerializer)

  override lazy val dateTimesMarshaller: Marshaller[Seq[org.joda.time.DateTime]] =
    ContentTypeBoundMarshallerFactory[Seq[org.joda.time.DateTime]](commonContentTypeProviders.dateTimesContentTypeProvider, DefaultMarshallingInstances.DateTimesMarshallingInst).marshaller(commonWireSerializers.dateTimesWireSerializer)

  override lazy val finiteDurationsMarshaller: Marshaller[Seq[scala.concurrent.duration.FiniteDuration]] =
    ContentTypeBoundMarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]](commonContentTypeProviders.finiteDurationsContentTypeProvider, DefaultMarshallingInstances.DurationsMarshallingInst).marshaller(commonWireSerializers.finiteDurationsWireSerializer)

  override lazy val eventMarshaller: Marshaller[almhirt.common.Event] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Event](commonContentTypeProviders.eventContentTypeProvider, DefaultMarshallingInstances.EventMarshallingInst).marshaller(commonWireSerializers.eventWireSerializer)

  override lazy val commandMarshaller: Marshaller[almhirt.common.Command] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Command](commonContentTypeProviders.commandContentTypeProvider, DefaultMarshallingInstances.CommandMarshallingInst).marshaller(commonWireSerializers.commandWireSerializer)

  override lazy val problemMarshaller: Marshaller[almhirt.common.Problem] =
    ContentTypeBoundMarshallerFactory[almhirt.common.Problem](commonContentTypeProviders.problemContentTypeProvider, DefaultMarshallingInstances.ProblemMarshallingInst).marshaller(commonWireSerializers.problemWireSerializer)

  override lazy val eventsMarshaller: Marshaller[Seq[almhirt.common.Event]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Event]](commonContentTypeProviders.eventsContentTypeProvider, DefaultMarshallingInstances.EventsMarshallingInst).marshaller(commonWireSerializers.eventsWireSerializer)

  override lazy val commandsMarshaller: Marshaller[Seq[almhirt.common.Command]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Command]](commonContentTypeProviders.commandsContentTypeProvider, DefaultMarshallingInstances.CommandsMarshallingInst).marshaller(commonWireSerializers.commandsWireSerializer)

  override lazy val problemsMarshaller: Marshaller[Seq[almhirt.common.Problem]] =
    ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Problem]](commonContentTypeProviders.problemsContentTypeProvider, DefaultMarshallingInstances.ProblemsMarshallingInst).marshaller(commonWireSerializers.problemsWireSerializer)

}