package almhirt.httpx.spray.marshalling

import almhirt.serialization.HasCommonWireSerializers

trait HasCommonMarshallers { self: HasCommonWireSerializers with HasCommonContentTypeProviders =>
  import DefaultMarshallingInstances._
  implicit val booleanMarshaller = ContentTypeBoundMarshallerFactory[Boolean].marshaller
  implicit val stringMarshaller = ContentTypeBoundMarshallerFactory[String].marshaller
  implicit val byteMarshaller = ContentTypeBoundMarshallerFactory[Byte].marshaller
  implicit val shortMarshaller = ContentTypeBoundMarshallerFactory[Short].marshaller
  implicit val intMarshaller = ContentTypeBoundMarshallerFactory[Int].marshaller
  implicit val longMarshaller = ContentTypeBoundMarshallerFactory[Long].marshaller
  implicit val bigIntMarshaller = ContentTypeBoundMarshallerFactory[BigInt].marshaller
  implicit val floatMarshaller = ContentTypeBoundMarshallerFactory[Float].marshaller
  implicit val doubleMarshaller = ContentTypeBoundMarshallerFactory[Double].marshaller
  implicit val bigDecimalMarshaller = ContentTypeBoundMarshallerFactory[BigDecimal].marshaller
  implicit val uriMarshaller = ContentTypeBoundMarshallerFactory[java.net.URI].marshaller
  implicit val uuidMarshaller = ContentTypeBoundMarshallerFactory[java.util.UUID].marshaller
  implicit val localDateTimeMarshaller = ContentTypeBoundMarshallerFactory[org.joda.time.LocalDateTime].marshaller
  implicit val dateTimeMarshaller = ContentTypeBoundMarshallerFactory[org.joda.time.DateTime].marshaller
  implicit val finiteDurationMarshaller = ContentTypeBoundMarshallerFactory[scala.concurrent.duration.FiniteDuration].marshaller

  implicit val booleansMarshaller = ContentTypeBoundMarshallerFactory[Seq[Boolean]].marshaller
  implicit val stringsMarshaller = ContentTypeBoundMarshallerFactory[Seq[String]].marshaller
  implicit val bytesMarshaller = ContentTypeBoundMarshallerFactory[Seq[Byte]].marshaller
  implicit val shortsMarshaller = ContentTypeBoundMarshallerFactory[Seq[Short]].marshaller
  implicit val intsMarshaller = ContentTypeBoundMarshallerFactory[Seq[Int]].marshaller
  implicit val longsMarshaller = ContentTypeBoundMarshallerFactory[Seq[Long]].marshaller
  implicit val bigIntsMarshaller = ContentTypeBoundMarshallerFactory[Seq[BigInt]].marshaller
  implicit val floatsMarshaller = ContentTypeBoundMarshallerFactory[Seq[Float]].marshaller
  implicit val doublesMarshaller = ContentTypeBoundMarshallerFactory[Seq[Double]].marshaller
  implicit val bigDecimalsMarshaller = ContentTypeBoundMarshallerFactory[Seq[BigDecimal]].marshaller
  implicit val urisMarshaller = ContentTypeBoundMarshallerFactory[Seq[java.net.URI]].marshaller
  implicit val uuidsMarshaller = ContentTypeBoundMarshallerFactory[Seq[java.util.UUID]].marshaller
  implicit val localDateTimesMarshaller = ContentTypeBoundMarshallerFactory[Seq[org.joda.time.LocalDateTime]].marshaller
  implicit val dateTimesMarshaller = ContentTypeBoundMarshallerFactory[Seq[org.joda.time.DateTime]].marshaller
  implicit val finiteDurationsMarshaller = ContentTypeBoundMarshallerFactory[Seq[scala.concurrent.duration.FiniteDuration]].marshaller

  implicit val eventMarshaller = ContentTypeBoundMarshallerFactory[almhirt.common.Event].marshaller
  implicit val commandMarshaller = ContentTypeBoundMarshallerFactory[almhirt.common.Command].marshaller
  implicit val problemMarshaller = ContentTypeBoundMarshallerFactory[almhirt.common.Problem].marshaller

  implicit val eventsMarshaller = ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Event]].marshaller
  implicit val commandsMarshaller = ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Command]].marshaller
  implicit val problemsMarshaller = ContentTypeBoundMarshallerFactory[Seq[almhirt.common.Problem]].marshaller
}