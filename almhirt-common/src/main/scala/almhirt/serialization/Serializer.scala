package almhirt.serialization

import almhirt.common._

trait WorksWithSerializedRepresentation {
  type SerializedRepr
}

trait WorksWithStringRepresentation {
  type SerializedRepr = String
}

trait WorksWithBinaryRepresentation {
  type SerializedRepr = Array[Byte]
}

trait CanSerialize[-TIn] extends WorksWithSerializedRepresentation {
  // (type, serialized)
  def serialize(channel: String)(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmValidation[(Option[String], SerializedRepr)]
  def serializeAsync(channel: String)(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmFuture[(Option[String], SerializedRepr)]
}

trait CanSerializeToFixedChannel[-TIn] extends WorksWithSerializedRepresentation {
  // (type, serialized)
  def channel: String
  def serialize(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmValidation[(Option[String], SerializedRepr)]
  def serializeAsync(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmFuture[(Option[String], SerializedRepr)]
}

trait CanDeserialize[+TOut] extends WorksWithSerializedRepresentation {
  def deserialize(channel: String)(what: SerializedRepr, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmValidation[TOut]
  def deserializeAsync(channel: String)(what: SerializedRepr, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmFuture[TOut]
}

trait CanDeserializeFromFixedChannel[+TOut] extends WorksWithSerializedRepresentation {
  def deserialize(what: SerializedRepr, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmValidation[TOut]
  def deserializeAsync(what: SerializedRepr, typeHint: Option[String], args: Map[String, Any] = Map.empty): AlmFuture[TOut]
}

object CanSerialize {
  implicit class CanSerializeOps[TIn](self: CanSerialize[TIn]) {
    def serializingToChannel(fixToThisChannel: String): CanSerializeToFixedChannel[TIn] =
      new CanSerializeToFixedChannel[TIn] {
        type SerializedRepr = self.SerializedRepr
        val channel = fixToThisChannel
        def serialize(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.serialize(channel)(what, typeHint, args)
        def serializeAsync(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.serializeAsync(channel)(what, typeHint, args)
      }
  }
}

trait CanSerializeAndDeserialize[-TIn, +TOut] extends CanSerialize[TIn] with CanDeserialize[TOut]
trait CanSerializeToFixedChannelAndDeserialize[-TIn, +TOut] extends CanSerializeToFixedChannel[TIn] with CanDeserialize[TOut]
trait CanSerializeAndDeserializeWithFixedChannel[-TIn, +TOut] extends CanSerializeToFixedChannel[TIn] with CanDeserializeFromFixedChannel[TOut]

trait StringSerializing[-TIn, +TOut] extends CanSerializeAndDeserialize[TIn, TOut] with WorksWithStringRepresentation
trait StringSerializingToFixedChannel[-TIn, +TOut] extends CanSerializeToFixedChannelAndDeserialize[TIn, TOut] with WorksWithStringRepresentation
trait BinarySerializing[-TIn, +TOut] extends CanSerializeAndDeserialize[TIn, TOut] with WorksWithBinaryRepresentation
trait BinarySerializingToFixedChannel[-TIn, +TOut] extends CanSerializeToFixedChannelAndDeserialize[TIn, TOut] with WorksWithBinaryRepresentation

object StringSerializing {
  implicit class StringSerializingOps[TIn, TOut](self: StringSerializing[TIn, TOut]) {
    def serializingToChannel(fixToThisChannel: String): StringSerializingToFixedChannel[TIn, TOut] =
      new StringSerializingToFixedChannel[TIn, TOut] {
        val channel = fixToThisChannel
        def serialize(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.serialize(channel)(what, typeHint, args)
        def serializeAsync(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.serializeAsync(channel)(what, typeHint, args)
        def deserialize(channel: String)(what: String, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.deserialize(channel)(what, typeHint, args)
        def deserializeAsync(channel: String)(what: String, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.deserializeAsync(channel)(what, typeHint, args)
      }
  }
}

object BinarySerializing {
  implicit class StringSerializingOps[TIn, TOut](self: BinarySerializing[TIn, TOut]) {
    def serializingToChannel(fixToThisChannel: String): BinarySerializingToFixedChannel[TIn, TOut] =
      new BinarySerializingToFixedChannel[TIn, TOut] {
        val channel = fixToThisChannel
        def serialize(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.serialize(channel)(what, typeHint, args)
        def serializeAsync(what: TIn, typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.serializeAsync(channel)(what, typeHint, args)
        def deserialize(channel: String)(what: Array[Byte], typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.deserialize(channel)(what, typeHint, args)
        def deserializeAsync(channel: String)(what: Array[Byte], typeHint: Option[String], args: Map[String, Any] = Map.empty) = self.deserializeAsync(channel)(what, typeHint, args)
      }
  }
}
