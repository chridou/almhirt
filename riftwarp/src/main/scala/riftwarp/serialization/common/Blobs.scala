package riftwarp.serialization.common

import almhirt.common._
import almhirt.serialization._
import riftwarp._

object BlobArrayValueDecomposer extends Decomposer[BlobArrayValue] {
  val riftDescriptor = RiftDescriptor(classOf[BlobArrayValue])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobArrayValue, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addByteArrayBlobEncoded("data", what.data).ok
  }
}

object BlobArrayValueRecomposer extends Recomposer[BlobArrayValue] {
  val riftDescriptor = RiftDescriptor(classOf[BlobArrayValue])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[BlobArrayValue] = {
    from.getByteArrayFromBlobEncoding("data").map(BlobArrayValue(_))
  }
}

object BlobValueDecomposer extends Decomposer[BlobValue] {
  val riftDescriptor = RiftDescriptor(classOf[BlobValue])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobValue, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case act : BlobArrayValue => into.includeDirect(act, BlobArrayValueDecomposer)
    }
  }
}

object BlobValueRecomposer extends DivertingRecomposer[BlobValue] {
  val riftDescriptor = RiftDescriptor(classOf[BlobValue])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      BlobArrayValueRecomposer.riftDescriptor -> BlobArrayValueRecomposer).lift
}


object BlobRefFilePathDecomposer extends Decomposer[BlobRefFilePath] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefFilePath])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobRefFilePath, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addString("path", what.path).ok
  }
}

object BlobRefFilePathRecomposer extends Recomposer[BlobRefFilePath] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefFilePath])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[BlobRefFilePath] = {
    from.getString("path").map(BlobRefFilePath(_))
  }
}

object BlobRefByNameDecomposer extends Decomposer[BlobRefByName] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefByName])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobRefByName, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addString("name", what.name).ok
  }
}

object BlobRefByNameRecomposer extends Recomposer[BlobRefByName] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefByName])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[BlobRefByName] = {
    from.getString("name").map(BlobRefByName(_))
  }
}

object BlobRefByUuidDecomposer extends Decomposer[BlobRefByUuid] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefByUuid])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobRefByUuid, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addUuid("uuid", what.uuid).ok
  }
}

object BlobRefByUuidRecomposer extends Recomposer[BlobRefByUuid] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefByUuid])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[BlobRefByUuid] = {
    from.getUuid("uuid").map(BlobRefByUuid(_))
  }
}

object BlobRefByUriDecomposer extends Decomposer[BlobRefByUri] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefByUri])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobRefByUri, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addUri("uri", what.uri).ok
  }
}

object BlobRefByUriRecomposer extends Recomposer[BlobRefByUri] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRefByUri])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[BlobRefByUri] = {
    from.getUri("uri").map(BlobRefByUri(_))
  }
}

object BlobReferenceDecomposer extends Decomposer[BlobReference] {
  val riftDescriptor = RiftDescriptor(classOf[BlobReference])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobReference, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case act : BlobRefFilePath => into.includeDirect(act, BlobRefFilePathDecomposer)
      case act : BlobRefByName => into.includeDirect(act, BlobRefByNameDecomposer)
      case act : BlobRefByUuid => into.includeDirect(act, BlobRefByUuidDecomposer)
      case act : BlobRefByUri => into.includeDirect(act, BlobRefByUriDecomposer)
    }
  }
}

object BlobReferenceRecomposer extends DivertingRecomposer[BlobReference] {
  val riftDescriptor = RiftDescriptor(classOf[BlobReference])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      BlobRefFilePathRecomposer.riftDescriptor -> BlobRefFilePathRecomposer,
      BlobRefByNameRecomposer.riftDescriptor -> BlobRefByNameRecomposer,
      BlobRefByUuidRecomposer.riftDescriptor -> BlobRefByUuidRecomposer,
      BlobRefByUriRecomposer.riftDescriptor -> BlobRefByUriRecomposer).lift
}

object BlobRepresentationDecomposer extends Decomposer[BlobRepresentation] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRepresentation])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: BlobRepresentation, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case act : BlobValue => into.includeDirect(act, BlobValueDecomposer)
      case act : BlobReference => into.includeDirect(act, BlobReferenceDecomposer)
    }
  }
}

object BlobRepresentationRecomposer extends DivertingRecomposer[BlobRepresentation] {
  val riftDescriptor = RiftDescriptor(classOf[BlobRepresentation])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      BlobArrayValueRecomposer.riftDescriptor -> BlobArrayValueRecomposer,
      BlobRefFilePathRecomposer.riftDescriptor -> BlobRefFilePathRecomposer,
      BlobRefByNameRecomposer.riftDescriptor -> BlobRefByNameRecomposer,
      BlobRefByUuidRecomposer.riftDescriptor -> BlobRefByUuidRecomposer,
      BlobRefByUriRecomposer.riftDescriptor -> BlobRefByUriRecomposer).lift
}