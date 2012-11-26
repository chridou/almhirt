package almhirt.riftwarp

import almhirt.common._

trait DematerializerFactory[TDimension <: RiftDimension] {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createDematerializer(implicit hasDecomposers: HasDecomposers, hasDematerializers: HasDematerializers): AlmValidation[Dematerializer[TDimension]]
}