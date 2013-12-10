package almhirt.httpx.spray

import almhirt.http._

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
  implicit def uriMediaTypesProvider: MediaTypesProvider[java.net.URI]
  implicit def uuidMediaTypesProvider: MediaTypesProvider[java.util.UUID]
  implicit def localDateTimeMediaTypesProvider: MediaTypesProvider[org.joda.time.LocalDateTime]
  implicit def dateTimeMediaTypesProvider: MediaTypesProvider[org.joda.time.DateTime]
  implicit def finiteDurationMediaTypesProvider: MediaTypesProvider[scala.concurrent.duration.FiniteDuration]

  implicit def booleansMediaTypesProvider: MediaTypesProvider[Seq[Boolean]]
  implicit def stringsMediaTypesProvider: MediaTypesProvider[Seq[String]]
  implicit def bytesMediaTypesProvider: MediaTypesProvider[Seq[Byte]]
  implicit def shortsMediaTypesProvider: MediaTypesProvider[Seq[Short]]
  implicit def intsMediaTypesProvider: MediaTypesProvider[Seq[Int]]
  implicit def longsMediaTypesProvider: MediaTypesProvider[Seq[Long]]
  implicit def bigIntsMediaTypesProvider: MediaTypesProvider[Seq[BigInt]]
  implicit def floatsMediaTypesProvider: MediaTypesProvider[Seq[Float]]
  implicit def doublesMediaTypesProvider: MediaTypesProvider[Seq[Double]]
  implicit def bigDecimalsMediaTypesProvider: MediaTypesProvider[Seq[BigDecimal]]
  implicit def urisMediaTypesProvider: MediaTypesProvider[Seq[java.net.URI]]
  implicit def uuidsMediaTypesProvider: MediaTypesProvider[Seq[java.util.UUID]]
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

trait DelegatingCommonMediaTypesProviders { self:  HasCommonMediaTypesProviders =>
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
  lazy val uriMediaTypesProvider = commmonMediaTypesProviders.uriMediaTypesProvider
  lazy val uuidMediaTypesProvider = commmonMediaTypesProviders.uuidMediaTypesProvider
  lazy val localDateTimeMediaTypesProvider = commmonMediaTypesProviders.localDateTimeMediaTypesProvider
  lazy val dateTimeMediaTypesProvider = commmonMediaTypesProviders.dateTimeMediaTypesProvider
  lazy val finiteDurationMediaTypesProvider = commmonMediaTypesProviders.finiteDurationMediaTypesProvider

  lazy val booleansMediaTypesProvider = commmonMediaTypesProviders.booleansMediaTypesProvider
  lazy val stringsMediaTypesProvider = commmonMediaTypesProviders.stringsMediaTypesProvider
  lazy val bytesMediaTypesProvider = commmonMediaTypesProviders.bytesMediaTypesProvider
  lazy val shortsMediaTypesProvider = commmonMediaTypesProviders.shortsMediaTypesProvider
  lazy val intsMediaTypesProvider = commmonMediaTypesProviders.intsMediaTypesProvider
  lazy val longsMediaTypesProvider = commmonMediaTypesProviders.longsMediaTypesProvider
  lazy val bigIntsMediaTypesProvider = commmonMediaTypesProviders.bigIntsMediaTypesProvider
  lazy val floatsMediaTypesProvider = commmonMediaTypesProviders.floatsMediaTypesProvider
  lazy val doublesMediaTypesProvider = commmonMediaTypesProviders.doublesMediaTypesProvider
  lazy val bigDecimalsMediaTypesProvider = commmonMediaTypesProviders.bigDecimalsMediaTypesProvider
  lazy val urisMediaTypesProvider = commmonMediaTypesProviders.urisMediaTypesProvider
  lazy val uuidsMediaTypesProvider = commmonMediaTypesProviders.uuidsMediaTypesProvider
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

trait VendorBasedCommonMediaTypesProviders { self : HasCommonMediaTypesProviders =>
  implicit def vendorProvider: MediaTypeVendorProvider
  override lazy val booleanMediaTypesProvider = MediaTypesProvider.defaults[Boolean]("Boolean")
  override lazy val stringMediaTypesProvider = MediaTypesProvider.defaults[String]("String")
  override lazy val byteMediaTypesProvider = MediaTypesProvider.defaults[Byte]("Byte")
  override lazy val shortMediaTypesProvider = MediaTypesProvider.defaults[Short]("Short")
  override lazy val intMediaTypesProvider = MediaTypesProvider.defaults[Int]("Int")
  override lazy val longMediaTypesProvider = MediaTypesProvider.defaults[Long]("Long")
  override lazy val bigIntMediaTypesProvider = MediaTypesProvider.defaults[BigInt]("BigInt")
  override lazy val floatMediaTypesProvider = MediaTypesProvider.defaults[Float]("Float")
  override lazy val doubleMediaTypesProvider = MediaTypesProvider.defaults[Double]("Double")
  override lazy val bigDecimalMediaTypesProvider = MediaTypesProvider.defaults[BigDecimal]("BigDecimal")
  override lazy val uriMediaTypesProvider = MediaTypesProvider.defaults[java.net.URI]("Uri")
  override lazy val uuidMediaTypesProvider = MediaTypesProvider.defaults[java.util.UUID]("Uuid")
  override lazy val localDateTimeMediaTypesProvider = MediaTypesProvider.defaults[org.joda.time.LocalDateTime]("LocalDateTime")
  override lazy val dateTimeMediaTypesProvider = MediaTypesProvider.defaults[org.joda.time.DateTime]("DateTime")
  override lazy val finiteDurationMediaTypesProvider = MediaTypesProvider.defaults[scala.concurrent.duration.FiniteDuration]("FiniteDuration")

  override lazy val booleansMediaTypesProvider = MediaTypesProvider.defaults[Seq[Boolean]]("Booleans")
  override lazy val stringsMediaTypesProvider = MediaTypesProvider.defaults[Seq[String]]("Strings")
  override lazy val bytesMediaTypesProvider = MediaTypesProvider.defaults[Seq[Byte]]("Bytes")
  override lazy val shortsMediaTypesProvider = MediaTypesProvider.defaults[Seq[Short]]("Shorts")
  override lazy val intsMediaTypesProvider = MediaTypesProvider.defaults[Seq[Int]]("Ints")
  override lazy val longsMediaTypesProvider = MediaTypesProvider.defaults[Seq[Long]]("Longs")
  override lazy val bigIntsMediaTypesProvider = MediaTypesProvider.defaults[Seq[BigInt]]("BigInts")
  override lazy val floatsMediaTypesProvider = MediaTypesProvider.defaults[Seq[Float]]("Floats")
  override lazy val doublesMediaTypesProvider = MediaTypesProvider.defaults[Seq[Double]]("Doubles")
  override lazy val bigDecimalsMediaTypesProvider = MediaTypesProvider.defaults[Seq[BigDecimal]]("BigDecimals")
  override lazy val urisMediaTypesProvider = MediaTypesProvider.defaults[Seq[java.net.URI]]("Uris")
  override lazy val uuidsMediaTypesProvider = MediaTypesProvider.defaults[Seq[java.util.UUID]]("Uuids")
  override lazy val localDateTimesMediaTypesProvider = MediaTypesProvider.defaults[Seq[org.joda.time.LocalDateTime]]("LocalDateTimes")
  override lazy val dateTimesMediaTypesProvider = MediaTypesProvider.defaults[Seq[org.joda.time.DateTime]]("DateTimes")
  override lazy val finiteDurationsMediaTypesProvider = MediaTypesProvider.defaults[Seq[scala.concurrent.duration.FiniteDuration]]("FiniteDurations")

  override lazy val eventMediaTypesProvider = MediaTypesProvider.defaults[almhirt.common.Event]("Event")
  override lazy val commandMediaTypesProvider = MediaTypesProvider.defaults[almhirt.common.Command]("Command")
  override lazy val problemMediaTypeProvider = MediaTypesProvider.defaults[almhirt.common.Problem]("Problem")

  override lazy val eventsMediaTypesProvider = MediaTypesProvider.defaults[Seq[almhirt.common.Event]]("Events")
  override lazy val commandsMediaTypesProvider = MediaTypesProvider.defaults[Seq[almhirt.common.Command]]("Commands")
  override lazy val problemsMediaTypesProvider = MediaTypesProvider.defaults[Seq[almhirt.common.Problem]]("Problems")

}