package almhirt.corex.spray.marshalling

import almhirt.commanding.ExecutionState
import almhirt.domain.DomainEvent
import almhirt.httpx.spray.marshalling.MarshallingFactory

object DomainEventMarshalling extends MarshallingFactory[DomainEvent]
object ExecutionStateMarshalling extends MarshallingFactory[ExecutionState]

object BooleanMarshalling extends MarshallingFactory[Boolean]
object StringMarshalling extends MarshallingFactory[String]
object ByteMarshalling extends MarshallingFactory[Byte]
object IntMarshalling extends MarshallingFactory[Int]
object ShortMarshalling extends MarshallingFactory[Short]
object LongMarshalling extends MarshallingFactory[Long]
object BigIntMarshalling extends MarshallingFactory[BigInt]
object FloatMarshalling extends MarshallingFactory[Float]
object DoubleMarshalling extends MarshallingFactory[Double]
object BigDecimalMarshalling extends MarshallingFactory[BigDecimal]
object UuidMarshalling extends MarshallingFactory[java.util.UUID]
object UriMarshalling extends MarshallingFactory[java.net.URI]
object DateTimeMarshalling extends MarshallingFactory[org.joda.time.DateTime]
object LocalDateTimeMarshalling extends MarshallingFactory[org.joda.time.LocalDateTime]
object DurationMarshalling extends MarshallingFactory[scala.concurrent.duration.FiniteDuration]
