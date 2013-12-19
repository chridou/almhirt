package almhirt.httpx.spray.marshalling

import almhirt.serialization.HasCommonWireSerializers
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
  implicit def bytesUnmarshaller : Unmarshaller[Seq[Byte]]
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
  implicit def commandUnmarshaller: Unmarshaller[almhirt.common.Command]
  implicit def problemUnmarshaller: Unmarshaller[almhirt.common.Problem]

  implicit def eventsUnmarshaller: Unmarshaller[Seq[almhirt.common.Event]]
  implicit def commandsUnmarshaller: Unmarshaller[Seq[almhirt.common.Command]]
  implicit def problemsUnmarshaller: Unmarshaller[Seq[almhirt.common.Problem]]
}

trait CommonUnmarshallerInstances {self : HasCommonUnmarshallers =>
  def commonWireSerializers: HasCommonWireSerializers
  def commonContentTypeProviders: HasCommonContentTypeProviders
  
  override lazy val booleanUnmarshaller : Unmarshaller[Boolean] = 
    ContentTypeBoundUnmarshallerFactory[Boolean](commonContentTypeProviders.booleanContentTypeProvider, DefaultMarshallingInstances.BooleanMarshallingInst).unmarshaller(commonWireSerializers.booleanWireSerializer)

  override lazy val  stringUnmarshaller: Unmarshaller[String] = 
    ContentTypeBoundUnmarshallerFactory[String](commonContentTypeProviders.stringContentTypeProvider, DefaultMarshallingInstances.StringMarshallingInst).unmarshaller(commonWireSerializers.stringWireSerializer)

  override lazy val  byteUnmarshaller: Unmarshaller[Byte] = 
    ContentTypeBoundUnmarshallerFactory[Byte](commonContentTypeProviders.byteContentTypeProvider, DefaultMarshallingInstances.ByteMarshallingInst).unmarshaller(commonWireSerializers.byteWireSerializer)

  override lazy val  shortUnmarshaller: Unmarshaller[Short] = 
    ContentTypeBoundUnmarshallerFactory[Short](commonContentTypeProviders.shortContentTypeProvider, DefaultMarshallingInstances.ShortMarshallingInst).unmarshaller(commonWireSerializers.shortWireSerializer)

  override lazy val  intUnmarshaller: Unmarshaller[Int] = 
    ContentTypeBoundUnmarshallerFactory[Int](commonContentTypeProviders.intContentTypeProvider, DefaultMarshallingInstances.IntMarshallingInst).unmarshaller(commonWireSerializers.intWireSerializer)

  override lazy val  longUnmarshaller: Unmarshaller[Long] = 
    ContentTypeBoundUnmarshallerFactory[Long](commonContentTypeProviders.longContentTypeProvider, DefaultMarshallingInstances.LongMarshallingInst).unmarshaller(commonWireSerializers.longWireSerializer)

  override lazy val  bigIntUnmarshaller: Unmarshaller[BigInt] = 
    ContentTypeBoundUnmarshallerFactory[BigInt](commonContentTypeProviders.bigIntContentTypeProvider, DefaultMarshallingInstances.BigIntMarshallingInst).unmarshaller(commonWireSerializers.bigIntWireSerializer)

  override lazy val  floatUnmarshaller: Unmarshaller[Float] = 
    ContentTypeBoundUnmarshallerFactory[Float](commonContentTypeProviders.floatContentTypeProvider, DefaultMarshallingInstances.FloatMarshallingInst).unmarshaller(commonWireSerializers.floatWireSerializer)

  override lazy val  doubleUnmarshaller: Unmarshaller[Double] = 
    ContentTypeBoundUnmarshallerFactory[Double](commonContentTypeProviders.doubleContentTypeProvider, DefaultMarshallingInstances.DoubleMarshallingInst).unmarshaller(commonWireSerializers.doubleWireSerializer)

  override lazy val  bigDecimalUnmarshaller: Unmarshaller[BigDecimal] = 
    ContentTypeBoundUnmarshallerFactory[BigDecimal](commonContentTypeProviders.bigDecimalContentTypeProvider, DefaultMarshallingInstances.BigDecimalMarshallingInst).unmarshaller(commonWireSerializers.bigDecimalWireSerializer)

  override lazy val  uriUnmarshaller: Unmarshaller[java.net.URI] = 
    ContentTypeBoundUnmarshallerFactory[java.net.URI](commonContentTypeProviders.uriContentTypeProvider, DefaultMarshallingInstances.UriMarshallingInst).unmarshaller(commonWireSerializers.uriWireSerializer)

  override lazy val  uuidUnmarshaller: Unmarshaller[java.util.UUID] = 
    ContentTypeBoundUnmarshallerFactory[java.util.UUID](commonContentTypeProviders.uuidContentTypeProvider, DefaultMarshallingInstances.UuidMarshallingInst).unmarshaller(commonWireSerializers.uuidWireSerializer)

  override lazy val  localDateTimeUnmarshaller: Unmarshaller[org.joda.time.LocalDateTime] = 
    ContentTypeBoundUnmarshallerFactory[org.joda.time.LocalDateTime](commonContentTypeProviders.localDateTimeContentTypeProvider, DefaultMarshallingInstances.LocalDateTimeMarshallingInst).unmarshaller(commonWireSerializers.localDateTimeWireSerializer)

  override lazy val  dateTimeUnmarshaller: Unmarshaller[org.joda.time.DateTime] = 
    ContentTypeBoundUnmarshallerFactory[org.joda.time.DateTime](commonContentTypeProviders.dateTimeContentTypeProvider, DefaultMarshallingInstances.DateTimeMarshallingInst).unmarshaller(commonWireSerializers.dateTimeWireSerializer)

  override lazy val  finiteDurationUnmarshaller: Unmarshaller[scala.concurrent.duration.FiniteDuration] = 
    ContentTypeBoundUnmarshallerFactory[scala.concurrent.duration.FiniteDuration](commonContentTypeProviders.finiteDurationTypeProvider, DefaultMarshallingInstances.DurationMarshallingInst).unmarshaller(commonWireSerializers.finiteDurationWireSerializer)


  override lazy val  booleansUnmarshaller: Unmarshaller[Seq[Boolean]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[Boolean]](commonContentTypeProviders.booleansContentTypeProvider, DefaultMarshallingInstances.BooleansMarshallingInst).unmarshaller(commonWireSerializers.booleansWireSerializer)

  override lazy val  stringsUnmarshaller: Unmarshaller[Seq[String]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[String]](commonContentTypeProviders.stringsContentTypeProvider, DefaultMarshallingInstances.StringsMarshallingInst).unmarshaller(commonWireSerializers.stringsWireSerializer)

  override lazy val  bytesUnmarshaller: Unmarshaller[Seq[Byte]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[Byte]](commonContentTypeProviders.bytesContentTypeProvider, DefaultMarshallingInstances.BytesMarshallingInst).unmarshaller(commonWireSerializers.bytesWireSerializer)

  override lazy val  shortsUnmarshaller: Unmarshaller[Seq[Short]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[Short]](commonContentTypeProviders.shortsContentTypeProvider, DefaultMarshallingInstances.ShortsMarshallingInst).unmarshaller(commonWireSerializers.shortsWireSerializer)

  override lazy val  intsUnmarshaller: Unmarshaller[Seq[Int]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[Int]](commonContentTypeProviders.intsContentTypeProvider, DefaultMarshallingInstances.IntsMarshallingInst).unmarshaller(commonWireSerializers.intsWireSerializer)

  override lazy val  longsUnmarshaller: Unmarshaller[Seq[Long]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[Long]](commonContentTypeProviders.longsContentTypeProvider, DefaultMarshallingInstances.LongsMarshallingInst).unmarshaller(commonWireSerializers.longsWireSerializer)

  override lazy val  bigIntsUnmarshaller: Unmarshaller[Seq[BigInt]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[BigInt]](commonContentTypeProviders.bigIntsContentTypeProvider, DefaultMarshallingInstances.BigIntsMarshallingInst).unmarshaller(commonWireSerializers.bigIntsWireSerializer)

  override lazy val  floatsUnmarshaller: Unmarshaller[Seq[Float]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[Float]](commonContentTypeProviders.floatsContentTypeProvider, DefaultMarshallingInstances.FloatsMarshallingInst).unmarshaller(commonWireSerializers.floatsWireSerializer)

  override lazy val  doublesUnmarshaller: Unmarshaller[Seq[Double]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[Double]](commonContentTypeProviders.doublesContentTypeProvider, DefaultMarshallingInstances.DoublesMarshallingInst).unmarshaller(commonWireSerializers.doublesWireSerializer)

  override lazy val  bigDecimalsUnmarshaller: Unmarshaller[Seq[BigDecimal]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[BigDecimal]](commonContentTypeProviders.bigDecimalsContentTypeProvider, DefaultMarshallingInstances.BigDecimalsMarshallingInst).unmarshaller(commonWireSerializers.bigDecimalsWireSerializer)

  override lazy val  urisUnmarshaller: Unmarshaller[Seq[java.net.URI]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[java.net.URI]](commonContentTypeProviders.urisContentTypeProvider, DefaultMarshallingInstances.UrisMarshallingInst).unmarshaller(commonWireSerializers.urisWireSerializer)

  override lazy val  uuidsUnmarshaller: Unmarshaller[Seq[java.util.UUID]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[java.util.UUID]](commonContentTypeProviders.uuidsContentTypeProvider, DefaultMarshallingInstances.UuidsMarshallingInst).unmarshaller(commonWireSerializers.uuidsWireSerializer)

  override lazy val  localDateTimesUnmarshaller: Unmarshaller[Seq[org.joda.time.LocalDateTime]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[org.joda.time.LocalDateTime]](commonContentTypeProviders.localDateTimesContentTypeProvider, DefaultMarshallingInstances.LocalDateTimesMarshallingInst).unmarshaller(commonWireSerializers.localDateTimesWireSerializer)

  override lazy val  dateTimesUnmarshaller: Unmarshaller[Seq[org.joda.time.DateTime]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[org.joda.time.DateTime]](commonContentTypeProviders.dateTimesContentTypeProvider, DefaultMarshallingInstances.DateTimesMarshallingInst).unmarshaller(commonWireSerializers.dateTimesWireSerializer)

  override lazy val  finiteDurationsUnmarshaller: Unmarshaller[Seq[scala.concurrent.duration.FiniteDuration]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]](commonContentTypeProviders.finiteDurationsContentTypeProvider, DefaultMarshallingInstances.DurationsMarshallingInst).unmarshaller(commonWireSerializers.finiteDurationsWireSerializer)


  override lazy val  eventUnmarshaller: Unmarshaller[almhirt.common.Event] = 
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Event](commonContentTypeProviders.eventContentTypeProvider, DefaultMarshallingInstances.EventMarshallingInst).unmarshaller(commonWireSerializers.eventWireSerializer)

  override lazy val  commandUnmarshaller: Unmarshaller[almhirt.common.Command] = 
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Command](commonContentTypeProviders.commandContentTypeProvider, DefaultMarshallingInstances.CommandMarshallingInst).unmarshaller(commonWireSerializers.commandWireSerializer)

  override lazy val  problemUnmarshaller: Unmarshaller[almhirt.common.Problem] = 
    ContentTypeBoundUnmarshallerFactory[almhirt.common.Problem](commonContentTypeProviders.problemContentTypeProvider, DefaultMarshallingInstances.ProblemMarshallingInst).unmarshaller(commonWireSerializers.problemWireSerializer)


  override lazy val  eventsUnmarshaller: Unmarshaller[Seq[almhirt.common.Event]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Event]](commonContentTypeProviders.eventsContentTypeProvider, DefaultMarshallingInstances.EventsMarshallingInst).unmarshaller(commonWireSerializers.eventsWireSerializer)

  override lazy val  commandsUnmarshaller: Unmarshaller[Seq[almhirt.common.Command]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Command]](commonContentTypeProviders.commandsContentTypeProvider, DefaultMarshallingInstances.CommandsMarshallingInst).unmarshaller(commonWireSerializers.commandsWireSerializer)

  override lazy val  problemsUnmarshaller: Unmarshaller[Seq[almhirt.common.Problem]] = 
    ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Problem]](commonContentTypeProviders.problemsContentTypeProvider, DefaultMarshallingInstances.ProblemsMarshallingInst).unmarshaller(commonWireSerializers.problemsWireSerializer)

}