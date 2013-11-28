package almhirt.httpx.spray.marshalling

import almhirt.serialization.HasCommonWireSerializers

trait HasCommonUnmarshallers { self: HasCommonWireSerializers with HasCommonContentTypeProviders =>
  import DefaultMarshallingInstances._
  implicit val booleanUnmarshaller = ContentTypeBoundUnmarshallerFactory[Boolean].unmarshaller
  implicit val stringUnmarshaller = ContentTypeBoundUnmarshallerFactory[String].unmarshaller
  implicit val byteUnmarshaller = ContentTypeBoundUnmarshallerFactory[Byte].unmarshaller
  implicit val shortUnmarshaller = ContentTypeBoundUnmarshallerFactory[Short].unmarshaller
  implicit val intUnmarshaller = ContentTypeBoundUnmarshallerFactory[Int].unmarshaller
  implicit val longUnmarshaller = ContentTypeBoundUnmarshallerFactory[Long].unmarshaller
  implicit val bigIntUnmarshaller = ContentTypeBoundUnmarshallerFactory[BigInt].unmarshaller
  implicit val floatUnmarshaller = ContentTypeBoundUnmarshallerFactory[Float].unmarshaller
  implicit val doubleUnmarshaller = ContentTypeBoundUnmarshallerFactory[Double].unmarshaller
  implicit val bigDecimalUnmarshaller = ContentTypeBoundUnmarshallerFactory[BigDecimal].unmarshaller
  implicit val uriUnmarshaller = ContentTypeBoundUnmarshallerFactory[java.net.URI].unmarshaller
  implicit val uuidUnmarshaller = ContentTypeBoundUnmarshallerFactory[java.util.UUID].unmarshaller
  implicit val localDateTimeUnmarshaller = ContentTypeBoundUnmarshallerFactory[org.joda.time.LocalDateTime].unmarshaller
  implicit val dateTimeUnmarshaller = ContentTypeBoundUnmarshallerFactory[org.joda.time.DateTime].unmarshaller
  implicit val finiteDurationUnmarshaller = ContentTypeBoundUnmarshallerFactory[scala.concurrent.duration.FiniteDuration].unmarshaller

  implicit val booleansUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[Boolean]].unmarshaller
  implicit val stringsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[String]].unmarshaller
  implicit val bytesUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[Byte]].unmarshaller
  implicit val shortsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[Short]].unmarshaller
  implicit val intsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[Int]].unmarshaller
  implicit val longsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[Long]].unmarshaller
  implicit val bigIntsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[BigInt]].unmarshaller
  implicit val floatsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[Float]].unmarshaller
  implicit val doublesUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[Double]].unmarshaller
  implicit val bigDecimalsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[BigDecimal]].unmarshaller
  implicit val urisUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[java.net.URI]].unmarshaller
  implicit val uuidsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[java.util.UUID]].unmarshaller
  implicit val localDateTimesUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[org.joda.time.LocalDateTime]].unmarshaller
  implicit val dateTimesUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[org.joda.time.DateTime]].unmarshaller
  implicit val finiteDurationsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]].unmarshaller

  implicit val eventUnmarshaller = ContentTypeBoundUnmarshallerFactory[almhirt.common.Event].unmarshaller
  implicit val commandUnmarshaller = ContentTypeBoundUnmarshallerFactory[almhirt.common.Command].unmarshaller
  implicit val problemUnmarshaller = ContentTypeBoundUnmarshallerFactory[almhirt.common.Problem].unmarshaller

  implicit val eventsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Event]].unmarshaller
  implicit val commandsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Command]].unmarshaller
  implicit val problemsUnmarshaller = ContentTypeBoundUnmarshallerFactory[Seq[almhirt.common.Problem]].unmarshaller
}