package almhirt.httpx.spray.marshalling

import almhirt.http.HasCommonAlmMediaTypesProviders

trait HasCommonContentTypeProviders {
  implicit def booleanContentTypeProvider: FullContentTypeProvider[Boolean]
  implicit def stringContentTypeProvider: FullContentTypeProvider[String]
  implicit def byteContentTypeProvider: FullContentTypeProvider[Byte]
  implicit def shortContentTypeProvider: FullContentTypeProvider[Short]
  implicit def intContentTypeProvider: FullContentTypeProvider[Int]
  implicit def longContentTypeProvider: FullContentTypeProvider[Long]
  implicit def bigIntContentTypeProvider: FullContentTypeProvider[BigInt]
  implicit def floatContentTypeProvider: FullContentTypeProvider[Float]
  implicit def doubleContentTypeProvider: FullContentTypeProvider[Double]
  implicit def bigDecimalContentTypeProvider: FullContentTypeProvider[BigDecimal]
  implicit def uriContentTypeProvider: FullContentTypeProvider[java.net.URI]
  implicit def uuidContentTypeProvider: FullContentTypeProvider[java.util.UUID]
  implicit def localDateTimeContentTypeProvider: FullContentTypeProvider[org.joda.time.LocalDateTime]
  implicit def dateTimeContentTypeProvider: FullContentTypeProvider[org.joda.time.DateTime]
  implicit def finiteDurationTypeProvider: FullContentTypeProvider[scala.concurrent.duration.FiniteDuration]

  implicit def booleansContentTypeProvider: FullContentTypeProvider[Seq[Boolean]]
  implicit def stringsContentTypeProvider: FullContentTypeProvider[Seq[String]]
  implicit def bytesContentTypeProvider: FullContentTypeProvider[Seq[Byte]]
  implicit def shortsContentTypeProvider: FullContentTypeProvider[Seq[Short]]
  implicit def intsContentTypeProvider: FullContentTypeProvider[Seq[Int]]
  implicit def longsContentTypeProvider: FullContentTypeProvider[Seq[Long]]
  implicit def bigIntsContentTypeProvider: FullContentTypeProvider[Seq[BigInt]]
  implicit def floatsContentTypeProvider: FullContentTypeProvider[Seq[Float]]
  implicit def doublesContentTypeProvider: FullContentTypeProvider[Seq[Double]]
  implicit def bigDecimalsContentTypeProvider: FullContentTypeProvider[Seq[BigDecimal]]
  implicit def urisContentTypeProvider: FullContentTypeProvider[Seq[java.net.URI]]
  implicit def uuidsContentTypeProvider: FullContentTypeProvider[Seq[java.util.UUID]]
  implicit def localDateTimesContentTypeProvider: FullContentTypeProvider[Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesContentTypeProvider: FullContentTypeProvider[Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsContentTypeProvider: FullContentTypeProvider[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventContentTypeProvider: FullContentTypeProvider[almhirt.common.Event]
  implicit def commandContentTypeProvider: FullContentTypeProvider[almhirt.common.Command]
  implicit def problemContentTypeProvider: FullContentTypeProvider[almhirt.common.Problem]

  implicit def eventsContentTypeProvider: FullContentTypeProvider[Seq[almhirt.common.Event]]
  implicit def commandsContentTypeProvider: FullContentTypeProvider[Seq[almhirt.common.Command]]
  implicit def problemsContentTypeProvider: FullContentTypeProvider[Seq[almhirt.common.Problem]]
}

trait EmptyCommonContentTypeProviders extends HasCommonContentTypeProviders {
  override val booleanContentTypeProvider = FullContentTypeProvider.empty[Boolean]
  override val stringContentTypeProvider = FullContentTypeProvider.empty[String]
  override val byteContentTypeProvider = FullContentTypeProvider.empty[Byte]
  override val shortContentTypeProvider = FullContentTypeProvider.empty[Short]
  override val intContentTypeProvider = FullContentTypeProvider.empty[Int]
  override val longContentTypeProvider = FullContentTypeProvider.empty[Long]
  override val bigIntContentTypeProvider = FullContentTypeProvider.empty[BigInt]
  override val floatContentTypeProvider = FullContentTypeProvider.empty[Float]
  override val doubleContentTypeProvider = FullContentTypeProvider.empty[Double]
  override val bigDecimalContentTypeProvider = FullContentTypeProvider.empty[BigDecimal]
  override val uriContentTypeProvider = FullContentTypeProvider.empty[java.net.URI]
  override val uuidContentTypeProvider = FullContentTypeProvider.empty[java.util.UUID]
  override val localDateTimeContentTypeProvider = FullContentTypeProvider.empty[org.joda.time.LocalDateTime]
  override val dateTimeContentTypeProvider = FullContentTypeProvider.empty[org.joda.time.DateTime]
  override val finiteDurationTypeProvider = FullContentTypeProvider.empty[scala.concurrent.duration.FiniteDuration]

  override val booleansContentTypeProvider = FullContentTypeProvider.empty[Seq[Boolean]]
  override val stringsContentTypeProvider = FullContentTypeProvider.empty[Seq[String]]
  override val bytesContentTypeProvider = FullContentTypeProvider.empty[Seq[Byte]]
  override val shortsContentTypeProvider = FullContentTypeProvider.empty[Seq[Short]]
  override val intsContentTypeProvider = FullContentTypeProvider.empty[Seq[Int]]
  override val longsContentTypeProvider = FullContentTypeProvider.empty[Seq[Long]]
  override val bigIntsContentTypeProvider = FullContentTypeProvider.empty[Seq[BigInt]]
  override val floatsContentTypeProvider = FullContentTypeProvider.empty[Seq[Float]]
  override val doublesContentTypeProvider = FullContentTypeProvider.empty[Seq[Double]]
  override val bigDecimalsContentTypeProvider = FullContentTypeProvider.empty[Seq[BigDecimal]]
  override val urisContentTypeProvider = FullContentTypeProvider.empty[Seq[java.net.URI]]
  override val uuidsContentTypeProvider = FullContentTypeProvider.empty[Seq[java.util.UUID]]
  override val localDateTimesContentTypeProvider = FullContentTypeProvider.empty[Seq[org.joda.time.LocalDateTime]]
  override val dateTimesContentTypeProvider = FullContentTypeProvider.empty[Seq[org.joda.time.DateTime]]
  override val finiteDurationsContentTypeProvider = FullContentTypeProvider.empty[Seq[scala.concurrent.duration.FiniteDuration]]

  override val eventContentTypeProvider = FullContentTypeProvider.empty[almhirt.common.Event]
  override val commandContentTypeProvider = FullContentTypeProvider.empty[almhirt.common.Command]
  override val problemContentTypeProvider = FullContentTypeProvider.empty[almhirt.common.Problem]

  override val eventsContentTypeProvider = FullContentTypeProvider.empty[Seq[almhirt.common.Event]]
  override val commandsContentTypeProvider = FullContentTypeProvider.empty[Seq[almhirt.common.Command]]
  override val problemsContentTypeProvider = FullContentTypeProvider.empty[Seq[almhirt.common.Problem]]
}

trait CommonContentTypeProvidersFromMediaTypes extends HasCommonContentTypeProviders { self: HasCommonAlmMediaTypesProviders =>
  override lazy val booleanContentTypeProvider = FullContentTypeProvider[Boolean]
  override lazy val stringContentTypeProvider = FullContentTypeProvider[String]
  override lazy val byteContentTypeProvider = FullContentTypeProvider[Byte]
  override lazy val shortContentTypeProvider = FullContentTypeProvider[Short]
  override lazy val intContentTypeProvider = FullContentTypeProvider[Int]
  override lazy val longContentTypeProvider = FullContentTypeProvider[Long]
  override lazy val bigIntContentTypeProvider = FullContentTypeProvider[BigInt]
  override lazy val floatContentTypeProvider = FullContentTypeProvider[Float]
  override lazy val doubleContentTypeProvider = FullContentTypeProvider[Double]
  override lazy val bigDecimalContentTypeProvider = FullContentTypeProvider[BigDecimal]
  override lazy val uriContentTypeProvider = FullContentTypeProvider[java.net.URI]
  override lazy val uuidContentTypeProvider = FullContentTypeProvider[java.util.UUID]
  override lazy val localDateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.LocalDateTime]
  override lazy val dateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.DateTime]
  override lazy val finiteDurationTypeProvider = FullContentTypeProvider[scala.concurrent.duration.FiniteDuration]

  override lazy val booleansContentTypeProvider = FullContentTypeProvider[Seq[Boolean]]
  override lazy val stringsContentTypeProvider = FullContentTypeProvider[Seq[String]]
  override lazy val bytesContentTypeProvider = FullContentTypeProvider[Seq[Byte]]
  override lazy val shortsContentTypeProvider = FullContentTypeProvider[Seq[Short]]
  override lazy val intsContentTypeProvider = FullContentTypeProvider[Seq[Int]]
  override lazy val longsContentTypeProvider = FullContentTypeProvider[Seq[Long]]
  override lazy val bigIntsContentTypeProvider = FullContentTypeProvider[Seq[BigInt]]
  override lazy val floatsContentTypeProvider = FullContentTypeProvider[Seq[Float]]
  override lazy val doublesContentTypeProvider = FullContentTypeProvider[Seq[Double]]
  override lazy val bigDecimalsContentTypeProvider = FullContentTypeProvider[Seq[BigDecimal]]
  override lazy val urisContentTypeProvider = FullContentTypeProvider[Seq[java.net.URI]]
  override lazy val uuidsContentTypeProvider = FullContentTypeProvider[Seq[java.util.UUID]]
  override lazy val localDateTimesContentTypeProvider = FullContentTypeProvider[Seq[org.joda.time.LocalDateTime]]
  override lazy val dateTimesContentTypeProvider = FullContentTypeProvider[Seq[org.joda.time.DateTime]]
  override lazy val finiteDurationsContentTypeProvider = FullContentTypeProvider[Seq[scala.concurrent.duration.FiniteDuration]]

  override lazy val eventContentTypeProvider = FullContentTypeProvider[almhirt.common.Event]
  override lazy val commandContentTypeProvider = FullContentTypeProvider[almhirt.common.Command]
  override lazy val problemContentTypeProvider = FullContentTypeProvider[almhirt.common.Problem]

  override lazy val eventsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Event]]
  override lazy val commandsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Command]]
  override lazy val problemsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Problem]]
}

trait DelegatingCommonContentTypeProvidersFromMediaTypes extends HasCommonContentTypeProviders {
  def hasCommonAlmMediaTypesProviders: HasCommonAlmMediaTypesProviders
  override lazy val booleanContentTypeProvider = FullContentTypeProvider[Boolean](hasCommonAlmMediaTypesProviders.booleanAlmMediaTypesProvider)
  override lazy val stringContentTypeProvider = FullContentTypeProvider[String](hasCommonAlmMediaTypesProviders.stringAlmMediaTypesProvider)
  override lazy val byteContentTypeProvider = FullContentTypeProvider[Byte](hasCommonAlmMediaTypesProviders.byteAlmMediaTypesProvider)
  override lazy val shortContentTypeProvider = FullContentTypeProvider[Short](hasCommonAlmMediaTypesProviders.shortAlmMediaTypesProvider)
  override lazy val intContentTypeProvider = FullContentTypeProvider[Int](hasCommonAlmMediaTypesProviders.intAlmMediaTypesProvider)
  override lazy val longContentTypeProvider = FullContentTypeProvider[Long](hasCommonAlmMediaTypesProviders.longAlmMediaTypesProvider)
  override lazy val bigIntContentTypeProvider = FullContentTypeProvider[BigInt](hasCommonAlmMediaTypesProviders.bigIntAlmMediaTypesProvider)
  override lazy val floatContentTypeProvider = FullContentTypeProvider[Float](hasCommonAlmMediaTypesProviders.floatAlmMediaTypesProvider)
  override lazy val doubleContentTypeProvider = FullContentTypeProvider[Double](hasCommonAlmMediaTypesProviders.doubleAlmMediaTypesProvider)
  override lazy val bigDecimalContentTypeProvider = FullContentTypeProvider[BigDecimal](hasCommonAlmMediaTypesProviders.bigDecimalAlmMediaTypesProvider)
  override lazy val uriContentTypeProvider = FullContentTypeProvider[java.net.URI](hasCommonAlmMediaTypesProviders.uriAlmMediaTypesProvider)
  override lazy val uuidContentTypeProvider = FullContentTypeProvider[java.util.UUID](hasCommonAlmMediaTypesProviders.uuidAlmMediaTypesProvider)
  override lazy val localDateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.LocalDateTime](hasCommonAlmMediaTypesProviders.localDateTimeAlmMediaTypesProvider)
  override lazy val dateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.DateTime](hasCommonAlmMediaTypesProviders.dateTimeAlmMediaTypesProvider)
  override lazy val finiteDurationTypeProvider = FullContentTypeProvider[scala.concurrent.duration.FiniteDuration](hasCommonAlmMediaTypesProviders.finiteDurationAlmMediaTypesProvider)

  override lazy val booleansContentTypeProvider = FullContentTypeProvider[Seq[Boolean]](hasCommonAlmMediaTypesProviders.booleansAlmMediaTypesProvider)
  override lazy val stringsContentTypeProvider = FullContentTypeProvider[Seq[String]](hasCommonAlmMediaTypesProviders.stringsAlmMediaTypesProvider)
  override lazy val bytesContentTypeProvider = FullContentTypeProvider[Seq[Byte]](hasCommonAlmMediaTypesProviders.bytesAlmMediaTypesProvider)
  override lazy val shortsContentTypeProvider = FullContentTypeProvider[Seq[Short]](hasCommonAlmMediaTypesProviders.shortsAlmMediaTypesProvider)
  override lazy val intsContentTypeProvider = FullContentTypeProvider[Seq[Int]](hasCommonAlmMediaTypesProviders.intsAlmMediaTypesProvider)
  override lazy val longsContentTypeProvider = FullContentTypeProvider[Seq[Long]](hasCommonAlmMediaTypesProviders.longsAlmMediaTypesProvider)
  override lazy val bigIntsContentTypeProvider = FullContentTypeProvider[Seq[BigInt]](hasCommonAlmMediaTypesProviders.bigIntsAlmMediaTypesProvider)
  override lazy val floatsContentTypeProvider = FullContentTypeProvider[Seq[Float]](hasCommonAlmMediaTypesProviders.floatsAlmMediaTypesProvider)
  override lazy val doublesContentTypeProvider = FullContentTypeProvider[Seq[Double]](hasCommonAlmMediaTypesProviders.doublesAlmMediaTypesProvider)
  override lazy val bigDecimalsContentTypeProvider = FullContentTypeProvider[Seq[BigDecimal]](hasCommonAlmMediaTypesProviders.bigDecimalsAlmMediaTypesProvider)
  override lazy val urisContentTypeProvider = FullContentTypeProvider[Seq[java.net.URI]](hasCommonAlmMediaTypesProviders.urisAlmMediaTypesProvider)
  override lazy val uuidsContentTypeProvider = FullContentTypeProvider[Seq[java.util.UUID]](hasCommonAlmMediaTypesProviders.uuidsAlmMediaTypesProvider)
  override lazy val localDateTimesContentTypeProvider = FullContentTypeProvider[Seq[org.joda.time.LocalDateTime]](hasCommonAlmMediaTypesProviders.localDateTimesAlmMediaTypesProvider)
  override lazy val dateTimesContentTypeProvider = FullContentTypeProvider[Seq[org.joda.time.DateTime]](hasCommonAlmMediaTypesProviders.dateTimesAlmMediaTypesProvider)
  override lazy val finiteDurationsContentTypeProvider = FullContentTypeProvider[Seq[scala.concurrent.duration.FiniteDuration]](hasCommonAlmMediaTypesProviders.finiteDurationsAlmMediaTypesProvider)

  override lazy val eventContentTypeProvider = FullContentTypeProvider[almhirt.common.Event](hasCommonAlmMediaTypesProviders.eventAlmMediaTypesProvider)
  override lazy val commandContentTypeProvider = FullContentTypeProvider[almhirt.common.Command](hasCommonAlmMediaTypesProviders.commandAlmMediaTypesProvider)
  override lazy val problemContentTypeProvider = FullContentTypeProvider[almhirt.common.Problem](hasCommonAlmMediaTypesProviders.problemAlmMediaTypesProvider)

  override lazy val eventsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Event]](hasCommonAlmMediaTypesProviders.eventsAlmMediaTypesProvider)
  override lazy val commandsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Command]](hasCommonAlmMediaTypesProviders.commandsAlmMediaTypesProvider)
  override lazy val problemsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Problem]](hasCommonAlmMediaTypesProviders.problemsAlmMediaTypesProvider)
}