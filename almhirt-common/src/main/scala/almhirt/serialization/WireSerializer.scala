package almhirt.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.http.AlmMediaType
import almhirt.http.AlmMediaTypesProvider

sealed trait WireRepresentation {
  def value: Any
}

final case class BinaryWire(value: Array[Byte]) extends WireRepresentation
final case class TextWire(value: String) extends WireRepresentation

trait CanSerializeToWire[-TIn] extends CanSerialize[TIn] with WorksWithWireRepresentation
trait CanDeserializeFromWire[+TOut] extends CanDeserialize[TOut] with WorksWithWireRepresentation

trait WireSerializer[T] extends CanSerializeToWire[T] with CanDeserializeFromWire[T] {
  final def marshallTo(mediaType: AlmMediaType)(what: T, options: Map[String, Any] = Map.empty): AlmValidation[WireRepresentation] =
    serialize(mediaType.contentFormat)(what, options).map(_._1)

  final def marshallDefault(what: T, options: Map[String, Any] = Map.empty)(implicit mp: AlmMediaTypesProvider[T]): AlmValidation[(WireRepresentation, AlmMediaType)] =
    mp.defaultForMarshalling match {
      case Some(mt) => marshallTo(mt)(what, options).map((_, mt))
      case None => NoSuchElementProblem("No default media type defined").failure
    }

  final def marshall(mediaType: AlmMediaType, what: T, options: Map[String, Any] = Map.empty)(implicit mp: AlmMediaTypesProvider[T]): AlmValidation[WireRepresentation] =
    if (mp.marshallableMediaTypes.contains(mediaType)) {
      marshallTo(mediaType)(what, options)
    } else {
      NoSuchElementProblem(s"""Media type "${mediaType.value}" is not supported for marshalling.""").failure
    }

  final def marshallToFormat(format: String, what: T, options: Map[String, Any] = Map.empty)(implicit mp: AlmMediaTypesProvider[T]): AlmValidation[(WireRepresentation, AlmMediaType)] =
    mp.getForMarshalling(format).flatMap(mt => marshallTo(mt)(what, options).map((_, mt)))

  final def marshallToTextFormat(format: String, what: T, options: Map[String, Any] = Map.empty)(implicit mp: AlmMediaTypesProvider[T]): AlmValidation[(String, AlmMediaType)] =
    mp.getForMarshalling(format).flatMap(mt =>
      if (mt.binary) {
        NoSuchElementProblem(s""""${mt.value}" is not a media type for textual wire transfer.""").failure
      } else {
        for {
          marshalled <- marshallTo(mt)(what, options)
          text <- marshalled match {
            case TextWire(txt) => txt.success
            case BinaryWire(_) => UnspecifiedProblem(s"""A crazy problem occured. A "text"("${mt.value}") media type serialized to binary!""").failure
          }
        } yield (text, mt)
      })

  final def marshallToBinaryFormat(format: String, what: T, options: Map[String, Any] = Map.empty)(implicit mp: AlmMediaTypesProvider[T]): AlmValidation[(Array[Byte], AlmMediaType)] =
    mp.getForMarshalling(format).flatMap(mt =>
      if (!mt.binary) {
        NoSuchElementProblem(s""""${mt.value}" is not a media type for binary wire transfer.""").failure
      } else {
        for {
          marshalled <- marshallTo(mt)(what, options)
          binary <- marshalled match {
            case BinaryWire(bin) => bin.success
            case TextWire(_) => UnspecifiedProblem(s"""A crazy problem occured. A "binary"("${mt.value}") media type serialized to text!""").failure
          }
        } yield (binary, mt)
      })

  final def unmarshallFrom(mediaType: AlmMediaType)(what: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[T] =
    deserialize(mediaType.contentFormat)(what, options)

  final def unmarshall(mediaType: AlmMediaType, what: WireRepresentation, options: Map[String, Any] = Map.empty)(implicit mp: AlmMediaTypesProvider[T]): AlmValidation[T] =
    if (mp.unmarshallableMediaTypes.contains(mediaType)) {
      unmarshallFrom(mediaType)(what, options)
    } else {
      NoSuchElementProblem(s"""Media type "${mediaType.value}" is not supported for unmarshalling.""").failure
    }

  final def unmarshallFromFormat(format: String, what: WireRepresentation, options: Map[String, Any] = Map.empty)(implicit mp: AlmMediaTypesProvider[T]): AlmValidation[(T, AlmMediaType)] =
    mp.getForUnmarshalling(format).flatMap(mt => unmarshallFrom(mt)(what, options).map((_, mt)))

}