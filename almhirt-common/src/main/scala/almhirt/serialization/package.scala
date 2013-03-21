package almhirt

package object serialization {
  type BlobStorageWithUuidBlobId = BlobStorage { type TBlobId = java.util.UUID }

}