package riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait DematerializerFactory[TDimension <: RiftDimension] {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createDematerializer(implicit hasDecomposers: HasDecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[Dematerializer[TDimension]] =
    createDematerializer((arr, _) => RiftBlobArrayValue(arr).success)
  def createDematerializer(divertBlob: (Array[Byte], List[String]) => AlmValidation[RiftBlob])(implicit hasDecomposers: HasDecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[Dematerializer[TDimension]]
}