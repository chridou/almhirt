package almhirt.serialization

import almhirt.common._

sealed trait WireRepresentation {
  def value: Any
}

final case class BinaryWire(value: Array[Byte]) extends WireRepresentation
final case class TextWire(value: String) extends WireRepresentation

trait WireSerializer[-TIn, +TOut] {
  def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(WireRepresentation, Option[String])]
  def deserialize(channel: String)(wire: WireRepresentation, options: Map[String, Any] = Map.empty): AlmValidation[TOut]
}