package riftwarp.util

import almhirt.common._
import almhirt.serialization._
import riftwarp.RiftWarp


class RiftSerializerOnStrings(riftWarp: RiftWarp, blobStorage: BlobStorage, minBlobSize: Int)(implicit support: HasExecutionContext with CanCreateUuidsAndDateTimes) extends Serializer[AnyRef] {
  type SerializedRepr = String

  def serialize(what: AnyRef): AlmValidation[(String, String, SerializedRepr)] =
    ???
    
  def serializeAsync(what: AnyRef): AlmFuture[(String, String, SerializedRepr)] =
    ???
  def deserialize(what: SerializedRepr, channel: String, typeIdent: String): AlmValidation[AnyRef] =
    ???
  def deserializeAsync(what: SerializedRepr, channel: String, typeIdent: String): AlmFuture[AnyRef] =
    ???
  
}