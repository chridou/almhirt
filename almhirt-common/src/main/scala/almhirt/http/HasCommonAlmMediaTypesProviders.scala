package almhirt.http

trait HasCommonAlmMediaTypesProviders {
  implicit def booleanAlmMediaTypesProvider: AlmMediaTypesProvider[Boolean]
  implicit def stringAlmMediaTypesProvider: AlmMediaTypesProvider[String]
  implicit def byteAlmMediaTypesProvider: AlmMediaTypesProvider[Byte]
  implicit def shortAlmMediaTypesProvider: AlmMediaTypesProvider[Short]
  implicit def intAlmMediaTypesProvider: AlmMediaTypesProvider[Int]
  implicit def longAlmMediaTypesProvider: AlmMediaTypesProvider[Long]
  implicit def bigIntAlmMediaTypesProvider: AlmMediaTypesProvider[BigInt]
  implicit def floatAlmMediaTypesProvider: AlmMediaTypesProvider[Float]
  implicit def doubleAlmMediaTypesProvider: AlmMediaTypesProvider[Double]
  implicit def bigDecimalAlmMediaTypesProvider: AlmMediaTypesProvider[BigDecimal]
  implicit def uriAlmMediaTypesProvider: AlmMediaTypesProvider[java.net.URI]
  implicit def uuidAlmMediaTypesProvider: AlmMediaTypesProvider[java.util.UUID]
  implicit def localDateTimeAlmMediaTypesProvider: AlmMediaTypesProvider[org.joda.time.LocalDateTime]
  implicit def dateTimeAlmMediaTypesProvider: AlmMediaTypesProvider[org.joda.time.DateTime]
  implicit def finiteDurationAlmMediaTypesProvider: AlmMediaTypesProvider[scala.concurrent.duration.FiniteDuration]

  implicit def booleansAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[Boolean]]
  implicit def stringsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[String]]
  implicit def bytesAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[Byte]]
  implicit def shortsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[Short]]
  implicit def intsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[Int]]
  implicit def longsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[Long]]
  implicit def bigIntsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[BigInt]]
  implicit def floatsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[Float]]
  implicit def doublesAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[Double]]
  implicit def bigDecimalsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[BigDecimal]]
  implicit def urisAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[java.net.URI]]
  implicit def uuidsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[java.util.UUID]]
  implicit def localDateTimesAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[org.joda.time.LocalDateTime]]
  implicit def dateTimesAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[org.joda.time.DateTime]]
  implicit def finiteDurationsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[scala.concurrent.duration.FiniteDuration]]

  implicit def eventAlmMediaTypesProvider: AlmMediaTypesProvider[almhirt.common.Event]
  implicit def commandAlmMediaTypesProvider: AlmMediaTypesProvider[almhirt.common.Command]
  implicit def problemAlmMediaTypesProvider: AlmMediaTypesProvider[almhirt.common.Problem]

  implicit def eventsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[almhirt.common.Event]]
  implicit def commandsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[almhirt.common.Command]]
  implicit def problemsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[almhirt.common.Problem]]
}

trait DelegatingCommonAlmMediaTypesProviders { self: HasCommonAlmMediaTypesProviders =>
  def commmonAlmMediaTypesProviders: HasCommonAlmMediaTypesProviders
  lazy val booleanAlmMediaTypesProvider = commmonAlmMediaTypesProviders.booleanAlmMediaTypesProvider
  lazy val stringAlmMediaTypesProvider = commmonAlmMediaTypesProviders.stringAlmMediaTypesProvider
  lazy val byteAlmMediaTypesProvider = commmonAlmMediaTypesProviders.byteAlmMediaTypesProvider
  lazy val shortAlmMediaTypesProvider = commmonAlmMediaTypesProviders.shortAlmMediaTypesProvider
  lazy val intAlmMediaTypesProvider = commmonAlmMediaTypesProviders.intAlmMediaTypesProvider
  lazy val longAlmMediaTypesProvider = commmonAlmMediaTypesProviders.longAlmMediaTypesProvider
  lazy val bigIntAlmMediaTypesProvider = commmonAlmMediaTypesProviders.bigIntAlmMediaTypesProvider
  lazy val floatAlmMediaTypesProvider = commmonAlmMediaTypesProviders.floatAlmMediaTypesProvider
  lazy val doubleAlmMediaTypesProvider = commmonAlmMediaTypesProviders.doubleAlmMediaTypesProvider
  lazy val bigDecimalAlmMediaTypesProvider = commmonAlmMediaTypesProviders.bigDecimalAlmMediaTypesProvider
  lazy val uriAlmMediaTypesProvider = commmonAlmMediaTypesProviders.uriAlmMediaTypesProvider
  lazy val uuidAlmMediaTypesProvider = commmonAlmMediaTypesProviders.uuidAlmMediaTypesProvider
  lazy val localDateTimeAlmMediaTypesProvider = commmonAlmMediaTypesProviders.localDateTimeAlmMediaTypesProvider
  lazy val dateTimeAlmMediaTypesProvider = commmonAlmMediaTypesProviders.dateTimeAlmMediaTypesProvider
  lazy val finiteDurationAlmMediaTypesProvider = commmonAlmMediaTypesProviders.finiteDurationAlmMediaTypesProvider

  lazy val booleansAlmMediaTypesProvider = commmonAlmMediaTypesProviders.booleansAlmMediaTypesProvider
  lazy val stringsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.stringsAlmMediaTypesProvider
  lazy val bytesAlmMediaTypesProvider = commmonAlmMediaTypesProviders.bytesAlmMediaTypesProvider
  lazy val shortsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.shortsAlmMediaTypesProvider
  lazy val intsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.intsAlmMediaTypesProvider
  lazy val longsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.longsAlmMediaTypesProvider
  lazy val bigIntsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.bigIntsAlmMediaTypesProvider
  lazy val floatsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.floatsAlmMediaTypesProvider
  lazy val doublesAlmMediaTypesProvider = commmonAlmMediaTypesProviders.doublesAlmMediaTypesProvider
  lazy val bigDecimalsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.bigDecimalsAlmMediaTypesProvider
  lazy val urisAlmMediaTypesProvider = commmonAlmMediaTypesProviders.urisAlmMediaTypesProvider
  lazy val uuidsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.uuidsAlmMediaTypesProvider
  lazy val localDateTimesAlmMediaTypesProvider = commmonAlmMediaTypesProviders.localDateTimesAlmMediaTypesProvider
  lazy val dateTimesAlmMediaTypesProvider = commmonAlmMediaTypesProviders.dateTimesAlmMediaTypesProvider
  lazy val finiteDurationsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.finiteDurationsAlmMediaTypesProvider

  lazy val eventAlmMediaTypesProvider = commmonAlmMediaTypesProviders.eventAlmMediaTypesProvider
  lazy val commandAlmMediaTypesProvider = commmonAlmMediaTypesProviders.commandAlmMediaTypesProvider
  lazy val problemAlmMediaTypesProvider = commmonAlmMediaTypesProviders.problemAlmMediaTypesProvider

  lazy val eventsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.eventsAlmMediaTypesProvider
  lazy val commandsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.commandsAlmMediaTypesProvider
  lazy val problemsAlmMediaTypesProvider = commmonAlmMediaTypesProviders.problemsAlmMediaTypesProvider
}

trait VendorBasedCommonAlmMediaTypesProviders { self : HasCommonAlmMediaTypesProviders =>
  implicit def vendorProvider: MediaTypeVendorProvider
  override lazy val booleanAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Boolean]("Boolean").withGenericTargets
  override lazy val stringAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[String]("String").withGenericTargets
  override lazy val byteAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Byte]("Byte").withGenericTargets
  override lazy val shortAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Short]("Short").withGenericTargets
  override lazy val intAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Int]("Int").withGenericTargets
  override lazy val longAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Long]("Long").withGenericTargets
  override lazy val bigIntAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[BigInt]("BigInt").withGenericTargets
  override lazy val floatAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Float]("Float").withGenericTargets
  override lazy val doubleAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Double]("Double").withGenericTargets
  override lazy val bigDecimalAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[BigDecimal]("BigDecimal").withGenericTargets
  override lazy val uriAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[java.net.URI]("Uri").withGenericTargets
  override lazy val uuidAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[java.util.UUID]("Uuid").withGenericTargets
  override lazy val localDateTimeAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[org.joda.time.LocalDateTime]("LocalDateTime").withGenericTargets
  override lazy val dateTimeAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[org.joda.time.DateTime]("DateTime").withGenericTargets
  override lazy val finiteDurationAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[scala.concurrent.duration.FiniteDuration]("FiniteDuration").withGenericTargets

  override lazy val booleansAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[Boolean]]("Booleans").withGenericTargets
  override lazy val stringsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[String]]("Strings").withGenericTargets
  override lazy val bytesAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[Byte]]("Bytes").withGenericTargets
  override lazy val shortsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[Short]]("Shorts").withGenericTargets
  override lazy val intsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[Int]]("Ints").withGenericTargets
  override lazy val longsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[Long]]("Longs").withGenericTargets
  override lazy val bigIntsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[BigInt]]("BigInts").withGenericTargets
  override lazy val floatsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[Float]]("Floats").withGenericTargets
  override lazy val doublesAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[Double]]("Doubles").withGenericTargets
  override lazy val bigDecimalsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[BigDecimal]]("BigDecimals").withGenericTargets
  override lazy val urisAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[java.net.URI]]("Uris").withGenericTargets
  override lazy val uuidsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[java.util.UUID]]("Uuids").withGenericTargets
  override lazy val localDateTimesAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[org.joda.time.LocalDateTime]]("LocalDateTimes").withGenericTargets
  override lazy val dateTimesAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[org.joda.time.DateTime]]("DateTimes").withGenericTargets
  override lazy val finiteDurationsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[scala.concurrent.duration.FiniteDuration]]("FiniteDurations").withGenericTargets

  override lazy val eventAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[almhirt.common.Event]("Event").withGenericTargets
  override lazy val commandAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[almhirt.common.Command]("Command").withGenericTargets
  override lazy val problemAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[almhirt.common.Problem]("Problem").withGenericTargets

  override lazy val eventsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[almhirt.common.Event]]("Events").withGenericTargets
  override lazy val commandsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[almhirt.common.Command]]("Commands").withGenericTargets
  override lazy val problemsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[almhirt.common.Problem]]("Problems").withGenericTargets

}