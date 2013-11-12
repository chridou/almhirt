package almhirt.serialization

import almhirt.common._

sealed trait WireRepresentation {
  def value: Any
}

final case class BinaryWire(value: Array[Byte]) extends WireRepresentation
final case class TextWire(value: String) extends WireRepresentation

trait CanSerializeToWire[-TIn]extends CanSerialize[TIn] with WorksWithWireRepresentation
trait CanDeserializeFromWire[+TOut]extends CanDeserialize[TOut] with WorksWithWireRepresentation

trait WireSerializer[-TIn, +TOut] extends CanSerializeToWire[TIn] with CanDeserializeFromWire[TOut]