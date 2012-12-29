package riftwarp

import almhirt.common._

trait RematerializerFactory[TDimension <: RiftDimension] {
  /**
   * Xml, Json, etc
   */
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createRematerializer(from: TDimension, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[Rematerializer]
  def createRematerializerRaw(from: AnyRef, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[Rematerializer] = createRematerializer(from.asInstanceOf[TDimension], fetchBlobs)(hasRecomposers, hasFunctionObject)
  def createRematerializer(from: TDimension)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[Rematerializer] = createRematerializer(from, NoFetchBlobFetch)(hasRecomposers, hasFunctionObject)
  def createRematerializerRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[Rematerializer] = createRematerializerRaw(from, NoFetchBlobFetch)(hasRecomposers, hasFunctionObject)
}
