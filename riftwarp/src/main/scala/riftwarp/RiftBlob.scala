package riftwarp

import scalaz.syntax.validation._
import almhirt.common._

sealed trait RiftBlob extends HasTypeDescriptor

trait RiftBlobValue extends RiftBlob {
  def dataAsArray: AlmValidation[Array[Byte]]
}


case class RiftBlobArrayValue(val data: Array[Byte]) extends RiftBlobValue {
  val typeDescriptor = TypeDescriptor("RiftBlobArrayValue")
  val dataAsArray = data.success
}

trait RiftBlobReference extends RiftBlob

case class RiftBlobRefFilePath(path : String) extends RiftBlobReference {
  val typeDescriptor = TypeDescriptor("RiftBlobRefFilePath")
}

case class RiftBlobRefByName(name : String) extends RiftBlobReference {
  val typeDescriptor = TypeDescriptor("RiftBlobRefByName")
}

case class RiftBlobRefByUri(uri : java.net.URI) extends RiftBlobReference {
  val typeDescriptor = TypeDescriptor("RiftBlobRefByUri")
}

object RiftBlob {
  
}