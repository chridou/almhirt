package riftwarp.util

import java.util.{ UUID => JUUID }
import scala.reflect.ClassTag
import almhirt.common._
import almhirt.serialization._
import riftwarp._

object Serializers {
  def createForStrings[T <: AnyRef](serializeTo: RiftChannel, riftWarp: RiftWarp)(implicit support: HasExecutionContext with CanCreateUuid, tag: ClassTag[T]): (Serializer[T], Deserializer[T]) =
    (new RiftSerializerOnString[T](serializeTo, riftWarp, None), new RiftDeserializerFromStrings[T](riftWarp, None))

  def createForStringsWithBlobs[T <: AnyRef](serializeTo: RiftChannel, riftWarp: RiftWarp, blobStorage: BlobStorage { type TBlobId = JUUID }, minBlobSize: Int)(implicit support: HasExecutionContext with CanCreateUuid, tag: ClassTag[T]): (Serializer[T], Deserializer[T]) =
    (new RiftSerializerOnString[T](serializeTo, riftWarp, Some((blobStorage, minBlobSize))), new RiftDeserializerFromStrings[T](riftWarp, Some(blobStorage)))
}