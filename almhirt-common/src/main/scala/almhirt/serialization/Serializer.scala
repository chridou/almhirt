package almhirt.serialization

import almhirt.common._

trait WorksWithSerializedRepresentation {
  type SerializedRepr
}

trait WorksWithStringRepresentation extends WorksWithSerializedRepresentation {
  override type SerializedRepr = String
}

trait WorksWithBinaryRepresentation extends WorksWithSerializedRepresentation {
  override type SerializedRepr = Array[Byte]
}

trait WorksWithWireRepresentation extends WorksWithSerializedRepresentation {
  override type SerializedRepr = WireRepresentation
}

trait CanSerialize[-TIn] extends WorksWithSerializedRepresentation {
  // (type, serialized)
  def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty): AlmValidation[(SerializedRepr, Option[String])]
}

trait CanDeserialize[+TOut] extends WorksWithSerializedRepresentation {
  def deserialize(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty): AlmValidation[TOut]
}

trait CanDeserializeFromFixedChannel[+TOut] extends WorksWithSerializedRepresentation {
  def deserialize(what: SerializedRepr, options: Map[String, Any] = Map.empty): AlmValidation[TOut]
}


trait CanSerializeAndDeserialize[-TIn, +TOut] extends CanSerialize[TIn] with CanDeserialize[TOut]

trait StringBasedSerializer[-TIn, +TOut] extends CanSerializeAndDeserialize[TIn, TOut] with WorksWithStringRepresentation
trait BinaryBasedSerializer[-TIn, +TOut] extends CanSerializeAndDeserialize[TIn, TOut] with WorksWithBinaryRepresentation
