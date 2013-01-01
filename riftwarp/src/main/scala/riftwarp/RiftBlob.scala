package riftwarp

import scalaz.syntax.validation._
import almhirt.common._

sealed trait RiftBlob extends HasTypeDescriptor with CanDecomposeSelf

trait RiftBlobValue extends RiftBlob {
  def dataAsArray: AlmValidation[Array[Byte]]
}

case class RiftBlobArrayValue(val data: Array[Byte]) extends RiftBlobValue {
  val typeDescriptor = TypeDescriptor("RiftBlobArrayValue")
  val dataAsArray = data.success

  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addTypeDescriptor(this.typeDescriptor).flatMap(demat =>
      demat.addByteArrayBlobEncoded("data", data))
}


trait RiftBlobReference extends RiftBlob

case class RiftBlobRefFilePath(path: String) extends RiftBlobReference {
  val typeDescriptor = TypeDescriptor("RiftBlobRefFilePath")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addTypeDescriptor(this.typeDescriptor).flatMap(demat =>
      demat.addString("path", path))
}

case class RiftBlobRefByName(name: String) extends RiftBlobReference {
  val typeDescriptor = TypeDescriptor("RiftBlobRefByName")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addTypeDescriptor(this.typeDescriptor).flatMap(demat =>
      demat.addString("name", name))
}

case class RiftBlobRefByUuid(uuid: java.util.UUID) extends RiftBlobReference {
  val typeDescriptor = TypeDescriptor("RiftBlobRefByUuid")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addTypeDescriptor(this.typeDescriptor).flatMap(demat =>
      demat.addUuid("uuid", uuid))
}

case class RiftBlobRefByUri(uri: java.net.URI) extends RiftBlobReference {
  val typeDescriptor = TypeDescriptor("RiftBlobRefByUri")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addTypeDescriptor(this.typeDescriptor).flatMap(demat =>
      demat.addUri("uri", uri))
}

object RiftBlob {
  def recompose(from: Rematerializer): AlmValidation[RiftBlob] =
    from.getTypeDescriptor.flatMap(td =>
      td match {
        case TypeDescriptor("RiftBlobArrayValue") =>
          from.getByteArrayFromBlobEncoding("data").map(RiftBlobArrayValue(_))
        case TypeDescriptor("RiftBlobRefFilePath") =>
          from.getString("path").map(RiftBlobRefFilePath(_))
        case TypeDescriptor("RiftBlobRefByName") =>
          from.getString("name").map(RiftBlobRefByName(_))
        case TypeDescriptor("RiftBlobRefByUuid") =>
          from.getUuid("uuid").map(RiftBlobRefByUuid(_))
        case TypeDescriptor("RiftBlobRefByUri") =>
          from.getUri("uri").map(RiftBlobRefByUri(_))
      })
}