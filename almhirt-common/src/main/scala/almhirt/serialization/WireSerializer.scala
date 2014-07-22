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

trait WireSerializer[T] {
  def serializeTo(what: T, mediaType: AlmMediaType)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[WireRepresentation]

  final def serialize(what: T)(implicit params: SerializationParams = SerializationParams.empty, mp: AlmMediaTypesProvider[T]): AlmValidation[(WireRepresentation, AlmMediaType)] =
    mp.defaultForSerialization match {
      case Some(mt) => serializeTo(what, mt).map((_, mt))
      case None => NoSuchElementProblem("No default media type defined").failure
    }

  final def serializeIfSupported(what: T, mediaType: AlmMediaType)(implicit params: SerializationParams = SerializationParams.empty, mp: AlmMediaTypesProvider[T]): AlmValidation[WireRepresentation] =
    if (mp.targetMediaTypes.contains(mediaType)) {
      serializeTo(what, mediaType)
    } else {
      NoSuchElementProblem(s"""Media type "${mediaType.value}" is not supported for wire serialization.""").failure
    }

  final def serializer(mediaType: AlmMediaType): SerializesToWire[T] =
    new SerializesToWire[T] {
      override def serialize(what: T)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[WireRepresentation] =
        serializeTo(what, mediaType)
    }
}

trait WireDeserializer[T] {
  def deserializeFrom(mediaType: AlmMediaType, what: WireRepresentation)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[T]

  final def deserialize(mediaType: AlmMediaType, what: WireRepresentation)(implicit params: SerializationParams = SerializationParams.empty, mp: AlmMediaTypesProvider[T]): AlmValidation[T] =
    if (mp.sourceMediaTypes.contains(mediaType)) {
      deserializeFrom(mediaType, what)
    } else {
      NoSuchElementProblem(s"""Media type "${mediaType.value}" is not supported for deserialization.""").failure
    }

  final def deserializer(mediaType: AlmMediaType): DeserializesFromWire[T] =
    new DeserializesFromWire[T] {
      def deserialize(what: WireRepresentation)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[T] =
        deserializeFrom(mediaType, what)
    }
}