package almhirt

import scalaz.syntax.validation._
import common._

package object serialization {
  type BlobStorageWithUuidBlobId = BlobStorage { type TBlobId = java.util.UUID }

  type BlobPacker = (Array[Byte], BlobIdentifier) => AlmValidation[BlobRepresentation] 
  type BlobUnpacker = (BlobReference) => AlmValidation[Array[Byte]] 
}