package riftwarp.components

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait DematerializerFactory[TDimension <: RiftDimension] {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createDematerializer(implicit hasDecomposers: HasDecomposers): AlmValidation[Dematerializer[TDimension]] =
    createDematerializer(NoDivertBlobDivert)(hasDecomposers)
  def createDematerializer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): AlmValidation[Dematerializer[TDimension]]
}