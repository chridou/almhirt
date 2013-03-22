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
  def serialize(channel: String)(what: TIn, typeHint: Option[String]): AlmValidation[(Option[String], SerializedRepr)]
  def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]): AlmFuture[(Option[String], SerializedRepr)]
}

trait CanSerializeToFixedChannel[-TIn] extends WorksWithSerializedRepresentation {
  // (type, serialized)
  def channel: String
  def serialize(what: TIn, typeHint: Option[String]): AlmValidation[(Option[String], SerializedRepr)]
  def serializeAsync(what: TIn, typeHint: Option[String]): AlmFuture[(Option[String], SerializedRepr)]
}

trait CanDeserialize[+TOut] extends WorksWithSerializedRepresentation {
  def deserialize(channel: String)(what: SerializedRepr, typeHint: Option[String]): AlmValidation[TOut]
  def deserializeAsync(channel: String)(what: SerializedRepr, typeHint: Option[String]): AlmFuture[TOut]
}

trait CanDeserializeFromFixedChannel[+TOut] extends WorksWithSerializedRepresentation {
  def deserialize(what: SerializedRepr, typeHint: Option[String]): AlmValidation[TOut]
  def deserializeAsync(what: SerializedRepr, typeHint: Option[String]): AlmFuture[TOut]
}

object CanSerialize {
  implicit class CanSerializeOps[T](self: CanSerialize[T]) {
    def bindToChannel(fixToThisChannel: String): CanSerializeToFixedChannel[T] =
      new CanSerializeToFixedChannel[T] {
        type SerializedRepr = self.SerializedRepr
        val channel = fixToThisChannel
        def serialize(what: T, typeHint: Option[String]) = self.serialize(channel)(what, typeHint)
        def serializeAsync(what: T, typeHint: Option[String]) = self.serializeAsync(channel)(what, typeHint)
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
    def serializeToChannel(fixToThisChannel: String): StringSerializingToFixedChannel[TIn, TOut] =
      new StringSerializingToFixedChannel[TIn, TOut] {
        val channel = fixToThisChannel
        def serialize(what: TIn, typeHint: Option[String]) = self.serialize(channel)(what, typeHint)
        def serializeAsync(what: TIn, typeHint: Option[String]) = self.serializeAsync(channel)(what, typeHint)
        def deserialize(channel: String)(what: String, typeHint: Option[String]) = self.deserialize(channel)(what, typeHint)
        def deserializeAsync(channel: String)(what: String, typeHint: Option[String]) = self.deserializeAsync(channel)(what, typeHint)
      }
  }
}

object BinarySerializing {
  implicit class StringSerializingOps[TIn, TOut](self: BinarySerializing[TIn, TOut]) {
    def serializeToChannel(fixToThisChannel: String): BinarySerializingToFixedChannel[TIn, TOut] =
      new BinarySerializingToFixedChannel[TIn, TOut] {
        val channel = fixToThisChannel
        def serialize(what: TIn, typeHint: Option[String]) = self.serialize(channel)(what, typeHint)
        def serializeAsync(what: TIn, typeHint: Option[String]) = self.serializeAsync(channel)(what, typeHint)
        def deserialize(channel: String)(what: Array[Byte], typeHint: Option[String]) = self.deserialize(channel)(what, typeHint)
        def deserializeAsync(channel: String)(what: Array[Byte], typeHint: Option[String]) = self.deserializeAsync(channel)(what, typeHint)
      }
  }
}

trait BlobStorage {
  type TBlobId
  def storeBlob(ident: TBlobId, data: Array[Byte]): AlmValidation[TBlobId]
  def storeBlobAsync(ident: TBlobId, data: Array[Byte]): AlmFuture[TBlobId]
  def fetchBlob(ident: TBlobId): AlmValidation[Array[Byte]]
  def fetchBlobAsync(ident: TBlobId): AlmFuture[Array[Byte]]
}

trait HasBlobStorageWithUuidBlobId { def blobStorege: BlobStorageWithUuidBlobId }
