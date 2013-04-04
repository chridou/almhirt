package riftwarp.components

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import almhirt.serialization._

trait WarpSequencerFactory[TDimension <: RiftDimension] {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createWarpSequencer(implicit hasDecomposers: HasDecomposers): AlmValidation[WarpSequencer[TDimension]] =
    createWarpSequencer(BlobSeparationDisabled)(hasDecomposers)
  def createWarpSequencer(blobPolicy: BlobSerializationPolicy)(implicit hasDecomposers: HasDecomposers): AlmValidation[WarpSequencer[TDimension]]
}