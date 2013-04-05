package almhirt.serialization

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._

object BlobPolicies {
  val disabled = BlobHandlingDisabled
  def uuidRefs(blobStorage: BlobStorage { type TBlobId = JUUID }, minBlobSize: Int = 0)(implicit ccuid: CanCreateUuid): BlobPolicy = {
    def pack(data: Array[Byte], ident: BlobIdentifier): AlmValidation[BlobRepresentation] =
      if (data.length <= minBlobSize)
        BlobArrayValue(data).success
      else
        BlobRefByUuid(ccuid.getUuid).success
    def unpack(ref: BlobReference): AlmValidation[Array[Byte]] =
      ref match {
        case BlobRefByUuid(uuid) => blobStorage.fetchBlob(uuid)
        case x => UnspecifiedSystemProblem(s""""${x.toString()}" is not a valid blob reference for a UUID policy""").failure
      }
      
    BlobHandlingEnabled(pack, unpack)
  }
}