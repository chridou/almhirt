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

  def decompose[TDimension <: RiftDimension](into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addByteArrayBlobEncoded("data", data).ok
}


trait RiftBlobReference extends RiftBlob

case class RiftBlobRefFilePath(path: String) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefFilePath")
  def decompose[TDimension <: RiftDimension](into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addString("path", path).ok
}

case class RiftBlobRefByName(name: String) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByName")
  def decompose[TDimension <: RiftDimension](into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addString("name", name).ok
}

case class RiftBlobRefByUuid(uuid: java.util.UUID) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByUuid")
  def decompose[TDimension <: RiftDimension](into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addUuid("uuid", uuid).ok
}

case class RiftBlobRefByUri(uri: java.net.URI) extends RiftBlobReference {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByUri")
  def decompose[TDimension <: RiftDimension](into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] =
    into.addRiftDescriptor(this.riftDescriptor).addUri("uri", uri).ok
}

object RiftBlobArrayValueRecomposer extends Recomposer[RiftBlobArrayValue] {
  val riftDescriptor = RiftDescriptor("RiftBlobArrayValue")
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[RiftBlobArrayValue] = {
    from.getByteArrayFromBlobEncoding("data").map(RiftBlobArrayValue(_))
  }
}

object RiftBlobRefFilePathRecomposer extends Recomposer[RiftBlobRefFilePath] {
  val riftDescriptor = RiftDescriptor("RiftBlobRefFilePath")
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[RiftBlobRefFilePath] = {
          from.getString("path").map(RiftBlobRefFilePath(_))
  }
}

object RiftBlobRefByNameRecomposer extends Recomposer[RiftBlobRefByName] {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByName")
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[RiftBlobRefByName] = {
          from.getString("name").map(RiftBlobRefByName(_))
  }
}

object RiftBlobRefByUuidRecomposer extends Recomposer[RiftBlobRefByUuid] {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByUuid")
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[RiftBlobRefByUuid] = {
          from.getUuid("uuid").map(RiftBlobRefByUuid(_))
  }
}

object RiftBlobRefByUriRecomposer extends Recomposer[RiftBlobRefByUri] {
  val riftDescriptor = RiftDescriptor("RiftBlobRefByUri")
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[RiftBlobRefByUri] = {
          from.getUri("uri").map(RiftBlobRefByUri(_))
  }
}

object RiftBlobRecomposer extends DivertingRecomposer[RiftBlob] {
  val riftDescriptor = RiftDescriptor(classOf[RiftBlob])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      RiftBlobArrayValueRecomposer.riftDescriptor -> RiftBlobArrayValueRecomposer,
      RiftBlobRefFilePathRecomposer.riftDescriptor -> RiftBlobRefFilePathRecomposer,
      RiftBlobRefByNameRecomposer.riftDescriptor -> RiftBlobRefByNameRecomposer,
      RiftBlobRefByUuidRecomposer.riftDescriptor -> RiftBlobRefByUuidRecomposer,
      RiftBlobRefByUriRecomposer.riftDescriptor -> RiftBlobRefByUriRecomposer).lift
}
