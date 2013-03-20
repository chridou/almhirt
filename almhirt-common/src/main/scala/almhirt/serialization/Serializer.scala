package almhirt.serialization

import almhirt.common._


trait WorksWithSerializedRepresentation {
  type SerializedRepr
}

trait Serializer[-TIn] extends WorksWithSerializedRepresentation{
  // (channel, type, serialized)
  def serialize(what: TIn): AlmValidation[(String, String, SerializedRepr)]
  def serializeAsync(what: TIn): AlmFuture[(String, String, SerializedRepr)]
}

trait Deserializer[+TOut] extends WorksWithSerializedRepresentation {
  def deserialize(what: SerializedRepr, channel: String, typeIdent: String): AlmValidation[TOut]
  def deserializeAsync(what: SerializedRepr, channel: String, typeIdent: String): AlmFuture[TOut]
}

trait BlobStorage {
  type TBlobId
  def storeBlob(ident: TBlobId, data: Array[Byte]): AlmValidation[TBlobId]
  def storeBlobAsync(ident: TBlobId, data: Array[Byte]): AlmFuture[TBlobId]
  def fetchBlob(ident: TBlobId): AlmValidation[Array[Byte]]
  def fetchBlobAsync(ident: TBlobId): AlmFuture[Array[Byte]]
}