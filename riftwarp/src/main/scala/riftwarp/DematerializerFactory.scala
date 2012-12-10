package riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait DematerializerFactory[TDimension <: RiftDimension] {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createDematerializer(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: ma.HasFunctionObjects): AlmValidation[Dematerializer[TDimension]] =
    createDematerializer(NoDivertBlobDivert)(hasDecomposers, hasFunctionObjects)
  def createDematerializer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: ma.HasFunctionObjects): AlmValidation[Dematerializer[TDimension]]
}