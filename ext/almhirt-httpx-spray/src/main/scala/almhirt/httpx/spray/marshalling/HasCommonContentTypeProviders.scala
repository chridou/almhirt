package almhirt.httpx.spray.marshalling

import almhirt.httpx.spray.HasCommonMediaTypesProviders

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
  implicit def uriWireContentTypeProvider: FullContentTypeProvider[java.net.URI]
  implicit def uuidWireContentTypeProvider: FullContentTypeProvider[java.util.UUID]
  implicit def localDateTimeContentTypeProvider: FullContentTypeProvider[org.joda.time.LocalDateTime]
  implicit def dateTimeContentTypeProvider: FullContentTypeProvider[org.joda.time.DateTime]
  implicit def finiteDurationTypeProvider: FullContentTypeProvider[scala.concurrent.duration.FiniteDuration]

  implicit def booleansContentTypeProvider: FullContentTypeProvider[Seq[Boolean]]
  implicit def stringsContentTypeProvider: FullContentTypeProvider[Seq[String]]
  implicit def bytesContentTypeProvider: FullContentTypeProvider[Seq[Byte]]
  implicit def shortsContentTypeProvider: FullContentTypeProvider[Seq[Short]]
  implicit def intsContentTypeProvider: FullContentTypeProvider[Seq[Int]]
  implicit def longsContentTypeProvider: FullContentTypeProvider[Seq[Long]]
  implicit def bigsIntContentTypeProvider: FullContentTypeProvider[Seq[BigInt]]
  implicit def floatsContentTypeProvider: FullContentTypeProvider[Seq[Float]]
  implicit def doublesContentTypeProvider: FullContentTypeProvider[Seq[Double]]
  implicit def bigsDecimalContentTypeProvider: FullContentTypeProvider[Seq[BigDecimal]]
  implicit def urisWireContentTypeProvider: FullContentTypeProvider[Seq[java.net.URI]]
  implicit def uuidsWireContentTypeProvider: FullContentTypeProvider[Seq[java.util.UUID]]
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
  override val uriWireContentTypeProvider = FullContentTypeProvider.empty[java.net.URI]
  override val uuidWireContentTypeProvider = FullContentTypeProvider.empty[java.util.UUID]
  override val localDateTimeContentTypeProvider = FullContentTypeProvider.empty[org.joda.time.LocalDateTime]
  override val dateTimeContentTypeProvider = FullContentTypeProvider.empty[org.joda.time.DateTime]
  override val finiteDurationTypeProvider = FullContentTypeProvider.empty[scala.concurrent.duration.FiniteDuration]

  override val booleansContentTypeProvider = FullContentTypeProvider.empty[Seq[Boolean]]
  override val stringsContentTypeProvider = FullContentTypeProvider.empty[Seq[String]]
  override val bytesContentTypeProvider = FullContentTypeProvider.empty[Seq[Byte]]
  override val shortsContentTypeProvider = FullContentTypeProvider.empty[Seq[Short]]
  override val intsContentTypeProvider = FullContentTypeProvider.empty[Seq[Int]]
  override val longsContentTypeProvider = FullContentTypeProvider.empty[Seq[Long]]
  override val bigsIntContentTypeProvider = FullContentTypeProvider.empty[Seq[BigInt]]
  override val floatsContentTypeProvider = FullContentTypeProvider.empty[Seq[Float]]
  override val doublesContentTypeProvider = FullContentTypeProvider.empty[Seq[Double]]
  override val bigsDecimalContentTypeProvider = FullContentTypeProvider.empty[Seq[BigDecimal]]
  override val urisWireContentTypeProvider = FullContentTypeProvider.empty[Seq[java.net.URI]]
  override val uuidsWireContentTypeProvider = FullContentTypeProvider.empty[Seq[java.util.UUID]]
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

trait CommonContentTypeProvidersFromMediaTypes extends HasCommonContentTypeProviders { self: HasCommonMediaTypesProviders =>
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
  override lazy val uriWireContentTypeProvider = FullContentTypeProvider[java.net.URI]
  override lazy val uuidWireContentTypeProvider = FullContentTypeProvider[java.util.UUID]
  override lazy val localDateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.LocalDateTime]
  override lazy val dateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.DateTime]
  override lazy val finiteDurationTypeProvider = FullContentTypeProvider[scala.concurrent.duration.FiniteDuration]

  override lazy val booleansContentTypeProvider = FullContentTypeProvider[Seq[Boolean]]
  override lazy val stringsContentTypeProvider = FullContentTypeProvider[Seq[String]]
  override lazy val bytesContentTypeProvider = FullContentTypeProvider[Seq[Byte]]
  override lazy val shortsContentTypeProvider = FullContentTypeProvider[Seq[Short]]
  override lazy val intsContentTypeProvider = FullContentTypeProvider[Seq[Int]]
  override lazy val longsContentTypeProvider = FullContentTypeProvider[Seq[Long]]
  override lazy val bigsIntContentTypeProvider = FullContentTypeProvider[Seq[BigInt]]
  override lazy val floatsContentTypeProvider = FullContentTypeProvider[Seq[Float]]
  override lazy val doublesContentTypeProvider = FullContentTypeProvider[Seq[Double]]
  override lazy val bigsDecimalContentTypeProvider = FullContentTypeProvider[Seq[BigDecimal]]
  override lazy val urisWireContentTypeProvider = FullContentTypeProvider[Seq[java.net.URI]]
  override lazy val uuidsWireContentTypeProvider = FullContentTypeProvider[Seq[java.util.UUID]]
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
  def hasCommonMediaTypesProviders: HasCommonMediaTypesProviders
  override lazy val booleanContentTypeProvider = FullContentTypeProvider[Boolean](hasCommonMediaTypesProviders.booleanMediaTypesProvider)
  override lazy val stringContentTypeProvider = FullContentTypeProvider[String](hasCommonMediaTypesProviders.stringMediaTypesProvider)
  override lazy val byteContentTypeProvider = FullContentTypeProvider[Byte](hasCommonMediaTypesProviders.byteMediaTypesProvider)
  override lazy val shortContentTypeProvider = FullContentTypeProvider[Short](hasCommonMediaTypesProviders.shortMediaTypesProvider)
  override lazy val intContentTypeProvider = FullContentTypeProvider[Int](hasCommonMediaTypesProviders.intMediaTypesProvider)
  override lazy val longContentTypeProvider = FullContentTypeProvider[Long](hasCommonMediaTypesProviders.longMediaTypesProvider)
  override lazy val bigIntContentTypeProvider = FullContentTypeProvider[BigInt](hasCommonMediaTypesProviders.bigIntMediaTypesProvider)
  override lazy val floatContentTypeProvider = FullContentTypeProvider[Float](hasCommonMediaTypesProviders.floatMediaTypesProvider)
  override lazy val doubleContentTypeProvider = FullContentTypeProvider[Double](hasCommonMediaTypesProviders.doubleMediaTypesProvider)
  override lazy val bigDecimalContentTypeProvider = FullContentTypeProvider[BigDecimal](hasCommonMediaTypesProviders.bigDecimalMediaTypesProvider)
  override lazy val uriWireContentTypeProvider = FullContentTypeProvider[java.net.URI](hasCommonMediaTypesProviders.uriMediaTypesProvider)
  override lazy val uuidWireContentTypeProvider = FullContentTypeProvider[java.util.UUID](hasCommonMediaTypesProviders.uuidMediaTypesProvider)
  override lazy val localDateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.LocalDateTime](hasCommonMediaTypesProviders.localDateTimeMediaTypesProvider)
  override lazy val dateTimeContentTypeProvider = FullContentTypeProvider[org.joda.time.DateTime](hasCommonMediaTypesProviders.dateTimeMediaTypesProvider)
  override lazy val finiteDurationTypeProvider = FullContentTypeProvider[scala.concurrent.duration.FiniteDuration](hasCommonMediaTypesProviders.finiteDurationMediaTypesProvider)

  override lazy val booleansContentTypeProvider = FullContentTypeProvider[Seq[Boolean]](hasCommonMediaTypesProviders.booleansMediaTypesProvider)
  override lazy val stringsContentTypeProvider = FullContentTypeProvider[Seq[String]](hasCommonMediaTypesProviders.stringsMediaTypesProvider)
  override lazy val bytesContentTypeProvider = FullContentTypeProvider[Seq[Byte]](hasCommonMediaTypesProviders.bytesMediaTypesProvider)
  override lazy val shortsContentTypeProvider = FullContentTypeProvider[Seq[Short]](hasCommonMediaTypesProviders.shortsMediaTypesProvider)
  override lazy val intsContentTypeProvider = FullContentTypeProvider[Seq[Int]](hasCommonMediaTypesProviders.intsMediaTypesProvider)
  override lazy val longsContentTypeProvider = FullContentTypeProvider[Seq[Long]](hasCommonMediaTypesProviders.longsMediaTypesProvider)
  override lazy val bigsIntContentTypeProvider = FullContentTypeProvider[Seq[BigInt]](hasCommonMediaTypesProviders.bigsIntMediaTypesProvider)
  override lazy val floatsContentTypeProvider = FullContentTypeProvider[Seq[Float]](hasCommonMediaTypesProviders.floatsMediaTypesProvider)
  override lazy val doublesContentTypeProvider = FullContentTypeProvider[Seq[Double]](hasCommonMediaTypesProviders.doublesMediaTypesProvider)
  override lazy val bigsDecimalContentTypeProvider = FullContentTypeProvider[Seq[BigDecimal]](hasCommonMediaTypesProviders.bigsDecimalMediaTypesProvider)
  override lazy val urisWireContentTypeProvider = FullContentTypeProvider[Seq[java.net.URI]](hasCommonMediaTypesProviders.urisMediaTypesProvider)
  override lazy val uuidsWireContentTypeProvider = FullContentTypeProvider[Seq[java.util.UUID]](hasCommonMediaTypesProviders.uuidsMediaTypesProvider)
  override lazy val localDateTimesContentTypeProvider = FullContentTypeProvider[Seq[org.joda.time.LocalDateTime]](hasCommonMediaTypesProviders.localDateTimesMediaTypesProvider)
  override lazy val dateTimesContentTypeProvider = FullContentTypeProvider[Seq[org.joda.time.DateTime]](hasCommonMediaTypesProviders.dateTimesMediaTypesProvider)
  override lazy val finiteDurationsContentTypeProvider = FullContentTypeProvider[Seq[scala.concurrent.duration.FiniteDuration]](hasCommonMediaTypesProviders.finiteDurationsMediaTypesProvider)

  override lazy val eventContentTypeProvider = FullContentTypeProvider[almhirt.common.Event](hasCommonMediaTypesProviders.eventMediaTypesProvider)
  override lazy val commandContentTypeProvider = FullContentTypeProvider[almhirt.common.Command](hasCommonMediaTypesProviders.commandMediaTypesProvider)
  override lazy val problemContentTypeProvider = FullContentTypeProvider[almhirt.common.Problem](hasCommonMediaTypesProviders.problemMediaTypeProvider)

  override lazy val eventsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Event]](hasCommonMediaTypesProviders.eventsMediaTypesProvider)
  override lazy val commandsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Command]](hasCommonMediaTypesProviders.commandsMediaTypesProvider)
  override lazy val problemsContentTypeProvider = FullContentTypeProvider[Seq[almhirt.common.Problem]](hasCommonMediaTypesProviders.problemsMediaTypesProvider)
}