package riftwarp.components

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait WarpSequencerFactory[TDimension <: RiftDimension] {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createWarpSequencer(implicit hasDecomposers: HasDecomposers): AlmValidation[WarpSequencer[TDimension]] =
    createWarpSequencer(NoDivertBlobDivert)(hasDecomposers)
  def createWarpSequencer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): AlmValidation[WarpSequencer[TDimension]]
}