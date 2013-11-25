package almhirt.httpx.spray.marshalling

import almhirt.common._

object EventMarshalling extends MarshallingFactory[Event]
object CommandMarshalling extends MarshallingFactory[Command]
object ProblemMarshalling extends MarshallingFactory[almhirt.common.Problem]

object BooleanMarshalling extends MarshallingFactory[Boolean]
object StringMarshalling extends MarshallingFactory[String]
object ByteMarshalling extends MarshallingFactory[Byte]
object ShortMarshalling extends MarshallingFactory[Short]
object IntMarshalling extends MarshallingFactory[Int]
object LongMarshalling extends MarshallingFactory[Long]
object BigIntMarshalling extends MarshallingFactory[BigInt]
object FloatMarshalling extends MarshallingFactory[Float]
object DoubleMarshalling extends MarshallingFactory[Double]
object BigDecimalMarshalling extends MarshallingFactory[BigDecimal]
object UriMarshalling extends MarshallingFactory[java.net.URI]
object UuidMarshalling extends MarshallingFactory[java.util.UUID]
object LocalDateTimeMarshalling extends MarshallingFactory[org.joda.time.LocalDateTime]
object DateTimeMarshalling extends MarshallingFactory[org.joda.time.DateTime]
object DurationMarshalling extends MarshallingFactory[scala.concurrent.duration.Duration]

object BooleansMarshalling extends MarshallingFactory[Seq[Boolean]]
object StringsMarshalling extends MarshallingFactory[Seq[String]]
object BytesMarshalling extends MarshallingFactory[Seq[Byte]]
object ShortsMarshalling extends MarshallingFactory[Seq[Short]]
object IntsMarshalling extends MarshallingFactory[Seq[Int]]
object LongsMarshalling extends MarshallingFactory[Seq[Long]]
object BigIntsMarshalling extends MarshallingFactory[Seq[BigInt]]
object FloatsMarshalling extends MarshallingFactory[Seq[Float]]
object DoublesMarshalling extends MarshallingFactory[Seq[Double]]
object BigDecimalsMarshalling extends MarshallingFactory[Seq[BigDecimal]]
object UrisMarshalling extends MarshallingFactory[Seq[java.net.URI]]
object UuidsMarshalling extends MarshallingFactory[Seq[java.util.UUID]]
object LocalDateTimesMarshalling extends MarshallingFactory[Seq[org.joda.time.LocalDateTime]]
object DateTimesMarshalling extends MarshallingFactory[Seq[org.joda.time.DateTime]]
object DurationsMarshalling extends MarshallingFactory[Seq[scala.concurrent.duration.Duration]]
