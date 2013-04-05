package almhirt.serialization

import almhirt.common._

trait BlobStorage {
  type TBlobId
  def storeBlob(ident: TBlobId, data: Array[Byte]): AlmValidation[TBlobId]
  def storeBlobAsync(ident: TBlobId, data: Array[Byte]): AlmFuture[TBlobId]
  def fetchBlob(ident: TBlobId): AlmValidation[Array[Byte]]
  def fetchBlobAsync(ident: TBlobId): AlmFuture[Array[Byte]]
}

trait HasBlobStorageWithUuidBlobId { def blobStorage: BlobStorageWithUuidBlobId }
