package almhirt.serialization

import almhirt.common._

trait WorksWithSerializedRepresentation {
  type SerializedRepr
}

trait CanSerialize[-TIn] extends WorksWithSerializedRepresentation {
  // (type, serialized)
  def serialize(channel: String)(what: TIn, typeHint: Option[String]): AlmValidation[(String, SerializedRepr)]
  def serializeAsync(channel: String)(what: TIn, typeHint: Option[String]): AlmFuture[(String, SerializedRepr)]
}


trait CanSerializeToFixedChannel[-TIn] extends WorksWithSerializedRepresentation {
  // (type, serialized)
  def channel: String
  def serialize(what: TIn, typeHint: Option[String]): AlmValidation[(String, SerializedRepr)]
  def serializeAsync(what: TIn, typeHint: Option[String]): AlmFuture[(String, SerializedRepr)]
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
trait CanSerializeAndDeserializeFixedToChannel[-TIn, +TOut] extends CanSerializeToFixedChannel[TIn] with CanDeserializeFromFixedChannel[TOut]

trait StringSerializing[-TIn, +TOut] extends CanSerializeAndDeserialize[TIn, TOut] { type SerializedRepr = String }
trait BinarySerializing[-TIn, +TOut] extends CanSerializeAndDeserialize[TIn, TOut] { type SerializedRepr = Array[Byte] }

trait BlobStorage {
  type TBlobId
  def storeBlob(ident: TBlobId, data: Array[Byte]): AlmValidation[TBlobId]
  def storeBlobAsync(ident: TBlobId, data: Array[Byte]): AlmFuture[TBlobId]
  def fetchBlob(ident: TBlobId): AlmValidation[Array[Byte]]
  def fetchBlobAsync(ident: TBlobId): AlmFuture[Array[Byte]]
}