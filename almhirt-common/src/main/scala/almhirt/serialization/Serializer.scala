package almhirt.serialization

import almhirt.common._

trait SerializationParams

object SerializationParams {
  val empty = new SerializationParams {}
}

trait Serializes[-TIn, TData] {
  def serialize(what: TIn)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[TData]
}

trait Deserializes[TData, +TOut] {
  def deserialize(what: TData)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[TOut]
}

trait SerializesToWire[-TIn] extends Serializes[TIn, WireRepresentation] {
  def serialize(what: TIn)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[WireRepresentation]
}

trait DeserializesFromWire[+TOut] extends Deserializes[WireRepresentation, TOut]{
  def deserialize(what: WireRepresentation)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[TOut]
}






