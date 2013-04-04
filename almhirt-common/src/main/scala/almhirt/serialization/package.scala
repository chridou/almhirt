package almhirt

import scalaz.syntax.validation._
import common._

package object serialization {
  type BlobStorageWithUuidBlobId = BlobStorage { type TBlobId = java.util.UUID }

  type BlobPacker = (Array[Byte], BlobIdentifier) => AlmValidation[BlobRepresentation] 
  type BlobUnpacker = (BlobRepresentation) => AlmValidation[Array[Byte]] 

  val BlobArrayValuePacker: BlobPacker = (arr: Array[Byte], path: BlobIdentifier) => BlobArrayValue(arr).success
  val BlobArrayValueUnpacker: BlobUnpacker = {
    case BlobArrayValue(arr) => arr.success 
    case x => UnspecifiedProblem("Could not fetch the blob's byte array. This is a standard function which can only fetch byte arrays from BlobArrayValue. Please specify your own function to retrieve blob data. The unsupprted type was: %s".format(x)).failure
  }
  
}