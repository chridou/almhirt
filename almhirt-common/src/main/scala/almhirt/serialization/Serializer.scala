package almhirt.serialization

import almhirt.common._

class SerializationParams private ()

object SerializationParams {
  val empty = new SerializationParams()
}

trait Serializes[-TIn, TData] {
  def serialize(what: TIn)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[TData]
}

trait Deserializes[TData, +TOut] {
  def deserialize(what: TData)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[TOut]
}

trait SerializesToString[-TIn] extends Serializes[TIn, String]
trait DeserializesFromString[+TOut] extends Deserializes[String, TOut]

trait SerializesToBytes[-TIn] extends Serializes[TIn, Array[Byte]]
trait DeserializesFromBytes[+TOut] extends Deserializes[Array[Byte], TOut]






