package almhirt.httpx.spray

trait HasCommonMediaTypesProviders {
  implicit def booleanMediaTypesProvider: MediaTypesProvider[Boolean]
  implicit def stringMediaTypesProvider: MediaTypesProvider[String]
  implicit def byteMediaTypesProvider: MediaTypesProvider[Byte]
  implicit def shortMediaTypesProvider: MediaTypesProvider[Short]
  implicit def intMediaTypesProvider: MediaTypesProvider[Int]
  implicit def longMediaTypesProvider: MediaTypesProvider[Long]
  implicit def bigIntMediaTypesProvider: MediaTypesProvider[BigInt]
  implicit def floatMediaTypesProvider: MediaTypesProvider[Float]
  implicit def doubleMediaTypesProvider: MediaTypesProvider[Double]
  implicit def bigDecimalMediaTypesProvider: MediaTypesProvider[BigDecimal]
  implicit def uriWireMediaTypesProvider: MediaTypesProvider[java.net.URI]
  implicit def uuidWireMediaTypesProvider: MediaTypesProvider[java.util.UUID]
  implicit def localDateTimeMediaTypesProvider: MediaTypesProvider[org.joda.time.LocalDateTime]
  implicit def dateTimeMediaTypesProvider: MediaTypesProvider[org.joda.time.DateTime]
  implicit def finiteDurationMediaTypesProvider: MediaTypesProvider[scala.concurrent.duration.FiniteDuration]

  implicit def booleansMediaTypesProvider: MediaTypesProvider[Seq[Boolean]]
  implicit def stringsMediaTypesProvider: MediaTypesProvider[Seq[String]]
  implicit def bytesMediaTypesProvider: MediaTypesProvider[Seq[Byte]]
  implicit def shortsMediaTypesProvider: MediaTypesProvider[Seq[Short]]
  implicit def intsMediaTypesProvider: MediaTypesProvider[Seq[Int]]
  implicit def longsMediaTypesProvider: MediaTypesProvider[Seq[Long]]
  implicit def bigsIntMediaTypesProvider: MediaTypesProvider[Seq[BigInt]]
  implicit def floatsMediaTypesProvider: MediaTypesProvider[Seq[Float]]
  implicit def doublesMediaTypesProvider: MediaTypesProvider[Seq[Double]]
  implicit def bigsDecimalMediaTypesProvider: MediaTypesProvider[Seq[BigDecimal]]
  implicit def urisWireMediaTypesProvider: MediaTypesProvider[Seq[java.net.URI]]
  implicit def uuidsWireMediaTypesProvider: MediaTypesProvider[Seq[java.util.UUID]]
  implicit def localDateTimesMediaTypesProvider: MediaTypesProvider[Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesMediaTypesProvider: MediaTypesProvider[Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsMediaTypesProvider: MediaTypesProvider[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventMediaTypesProvider: MediaTypesProvider[almhirt.common.Event]
  implicit def commandMediaTypesProvider: MediaTypesProvider[almhirt.common.Command]
  implicit def problemMediaTypeProvider: MediaTypesProvider[almhirt.common.Problem]

  implicit def eventsMediaTypesProvider: MediaTypesProvider[Seq[almhirt.common.Event]]
  implicit def commandsMediaTypesProvider: MediaTypesProvider[Seq[almhirt.common.Command]]
  implicit def problemsMediaTypesProvider: MediaTypesProvider[Seq[almhirt.common.Problem]]
}

trait DelegatingCommonMediaTypesProviders extends HasCommonMediaTypesProviders {
  def commmonMediaTypesProviders: HasCommonMediaTypesProviders
  lazy val booleanMediaTypesProvider = commmonMediaTypesProviders.booleanMediaTypesProvider
  lazy val stringMediaTypesProvider = commmonMediaTypesProviders.stringMediaTypesProvider
  lazy val byteMediaTypesProvider = commmonMediaTypesProviders.byteMediaTypesProvider
  lazy val shortMediaTypesProvider = commmonMediaTypesProviders.shortMediaTypesProvider
  lazy val intMediaTypesProvider = commmonMediaTypesProviders.intMediaTypesProvider
  lazy val longMediaTypesProvider = commmonMediaTypesProviders.longMediaTypesProvider
  lazy val bigIntMediaTypesProvider = commmonMediaTypesProviders.bigIntMediaTypesProvider
  lazy val floatMediaTypesProvider = commmonMediaTypesProviders.floatMediaTypesProvider
  lazy val doubleMediaTypesProvider = commmonMediaTypesProviders.doubleMediaTypesProvider
  lazy val bigDecimalMediaTypesProvider = commmonMediaTypesProviders.bigDecimalMediaTypesProvider
  lazy val uriWireMediaTypesProvider = commmonMediaTypesProviders.uriWireMediaTypesProvider
  lazy val uuidWireMediaTypesProvider = commmonMediaTypesProviders.uuidWireMediaTypesProvider
  lazy val localDateTimeMediaTypesProvider = commmonMediaTypesProviders.localDateTimeMediaTypesProvider
  lazy val dateTimeMediaTypesProvider = commmonMediaTypesProviders.dateTimeMediaTypesProvider
  lazy val finiteDurationMediaTypesProvider = commmonMediaTypesProviders.finiteDurationMediaTypesProvider

  lazy val booleansMediaTypesProvider = commmonMediaTypesProviders.booleansMediaTypesProvider
  lazy val stringsMediaTypesProvider = commmonMediaTypesProviders.stringsMediaTypesProvider
  lazy val bytesMediaTypesProvider = commmonMediaTypesProviders.bytesMediaTypesProvider
  lazy val shortsMediaTypesProvider = commmonMediaTypesProviders.shortsMediaTypesProvider
  lazy val intsMediaTypesProvider = commmonMediaTypesProviders.intsMediaTypesProvider
  lazy val longsMediaTypesProvider = commmonMediaTypesProviders.longsMediaTypesProvider
  lazy val bigsIntMediaTypesProvider = commmonMediaTypesProviders.bigsIntMediaTypesProvider
  lazy val floatsMediaTypesProvider = commmonMediaTypesProviders.floatsMediaTypesProvider
  lazy val doublesMediaTypesProvider = commmonMediaTypesProviders.doublesMediaTypesProvider
  lazy val bigsDecimalMediaTypesProvider = commmonMediaTypesProviders.bigsDecimalMediaTypesProvider
  lazy val urisWireMediaTypesProvider = commmonMediaTypesProviders.urisWireMediaTypesProvider
  lazy val uuidsWireMediaTypesProvider = commmonMediaTypesProviders.uuidsWireMediaTypesProvider
  lazy val localDateTimesMediaTypesProvider = commmonMediaTypesProviders.localDateTimesMediaTypesProvider
  lazy val dateTimesMediaTypesProvider = commmonMediaTypesProviders.dateTimesMediaTypesProvider
  lazy val finiteDurationsMediaTypesProvider = commmonMediaTypesProviders.finiteDurationsMediaTypesProvider

  lazy val eventMediaTypesProvider = commmonMediaTypesProviders.eventMediaTypesProvider
  lazy val commandMediaTypesProvider = commmonMediaTypesProviders.commandMediaTypesProvider
  lazy val problemMediaTypeProvider = commmonMediaTypesProviders.problemMediaTypeProvider

  lazy val eventsMediaTypesProvider = commmonMediaTypesProviders.eventsMediaTypesProvider
  lazy val commandsMediaTypesProvider = commmonMediaTypesProviders.commandsMediaTypesProvider
  lazy val problemsMediaTypesProvider = commmonMediaTypesProviders.problemsMediaTypesProvider

}

trait VendorBasedCommonMediaTypesProviders extends HasCommonMediaTypesProviders {
  implicit def vendorProvider: MediaTypeVendorProvider
  override val booleanMediaTypesProvider = MediaTypesProvider.registeredDefaults[Boolean]("Boolean")
  override val stringMediaTypesProvider = MediaTypesProvider.registeredDefaults[String]("String")
  override val byteMediaTypesProvider = MediaTypesProvider.registeredDefaults[Byte]("Byte")
  override val shortMediaTypesProvider = MediaTypesProvider.registeredDefaults[Short]("Short")
  override val intMediaTypesProvider = MediaTypesProvider.registeredDefaults[Int]("Int")
  override val longMediaTypesProvider = MediaTypesProvider.registeredDefaults[Long]("Long")
  override val bigIntMediaTypesProvider = MediaTypesProvider.registeredDefaults[BigInt]("BigInt")
  override val floatMediaTypesProvider = MediaTypesProvider.registeredDefaults[Float]("Float")
  override val doubleMediaTypesProvider = MediaTypesProvider.registeredDefaults[Double]("Double")
  override val bigDecimalMediaTypesProvider = MediaTypesProvider.registeredDefaults[BigDecimal]("BigDecimal")
  override val uriWireMediaTypesProvider = MediaTypesProvider.registeredDefaults[java.net.URI]("Uri")
  override val uuidWireMediaTypesProvider = MediaTypesProvider.registeredDefaults[java.util.UUID]("Uuid")
  override val localDateTimeMediaTypesProvider = MediaTypesProvider.registeredDefaults[org.joda.time.LocalDateTime]("LocalDateTime")
  override val dateTimeMediaTypesProvider = MediaTypesProvider.registeredDefaults[org.joda.time.DateTime]("DateTime")
  override val finiteDurationMediaTypesProvider = MediaTypesProvider.registeredDefaults[scala.concurrent.duration.FiniteDuration]("FiniteDuration")

  override val booleansMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[Boolean]]("Booleans")
  override val stringsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[String]]("Strings")
  override val bytesMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[Byte]]("Bytes")
  override val shortsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[Short]]("Shorts")
  override val intsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[Int]]("Ints")
  override val longsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[Long]]("Longs")
  override val bigsIntMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[BigInt]]("BigInts")
  override val floatsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[Float]]("Floats")
  override val doublesMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[Double]]("Doubles")
  override val bigsDecimalMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[BigDecimal]]("BigDecimals")
  override val urisWireMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[java.net.URI]]("Uris")
  override val uuidsWireMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[java.util.UUID]]("Uuids")
  override val localDateTimesMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[org.joda.time.LocalDateTime]]("LocalDateTimes")
  override val dateTimesMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[org.joda.time.DateTime]]("DateTimes")
  override val finiteDurationsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[scala.concurrent.duration.FiniteDuration]]("FiniteDurations")

  override val eventMediaTypesProvider = MediaTypesProvider.registeredDefaults[almhirt.common.Event]("Event")
  override val commandMediaTypesProvider = MediaTypesProvider.registeredDefaults[almhirt.common.Command]("Command")
  override val problemMediaTypeProvider = MediaTypesProvider.registeredDefaults[almhirt.common.Problem]("Problem")

  override val eventsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[almhirt.common.Event]]("Events")
  override val commandsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[almhirt.common.Command]]("Commands")
  override val problemsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[almhirt.common.Problem]]("Problems")

}