package riftwarp

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.components._

sealed trait RiftBlob extends HasRiftDescriptor with CanDecomposeSelf

trait RiftBlobValue extends RiftBlob {
  def dataAsArray: AlmValidation[Array[Byte]]
}

case class RiftBlobArrayValue(val data: Array[Byte]) extends RiftBlobValue {
  val riftDescriptor = RiftDescriptor("RiftBlobArrayValue")
  val dataAsArray = data.success

  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addByteArrayBlobEncoded("data", data).ok
}


trait RiftBlobReference extends RiftBlob

case class RiftBlobRefFilePath(path: String) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefFilePath")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addString("path", path).ok
}

case class RiftBlobRefByName(name: String) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByName")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addString("name", name).ok
}

case class RiftBlobRefByUuid(uuid: java.util.UUID) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByUuid")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addUuid("uuid", uuid).ok
}

case class RiftBlobRefByUri(uri: java.net.URI) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByUri")
  def decompose[TDimension <: RiftDimension](into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addUri("uri", uri).ok
}

object RiftBlob {
  def recompose(from: Rematerializer): AlmValidation[RiftBlob] =
    from.getRiftDescriptor.flatMap(td =>
      td match {
        case RiftDescriptor("RiftBlobArrayValue") =>
          from.getByteArrayFromBlobEncoding("data").map(RiftBlobArrayValue(_))
        case RiftDescriptor("RiftBlobRefFilePath") =>
          from.getString("path").map(RiftBlobRefFilePath(_))
        case RiftDescriptor("RiftBlobRefByName") =>
          from.getString("name").map(RiftBlobRefByName(_))
        case RiftDescriptor("RiftBlobRefByUuid") =>
          from.getUuid("uuid").map(RiftBlobRefByUuid(_))
        case RiftDescriptor("RiftBlobRefByUri") =>
          from.getUri("uri").map(RiftBlobRefByUri(_))
      })
}