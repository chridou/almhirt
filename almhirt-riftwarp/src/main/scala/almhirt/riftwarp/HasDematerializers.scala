package almhirt.riftwarp

trait HasDematerializers {
  def addDematerializer[D <: Dematerializer[_,_], TChannel <: RiftChannel, TDimension <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel,TDimension], asChannelDefault: Boolean = false)
  def addDematerializerAsDefault[D <: Dematerializer[_,_], TChannel <: RiftChannel, TDimension <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel,TDimension]) = addDematerializer(dematerializer, true)

  def tryGetDematerializerByDescriptor[TChannel <: RiftChannel, TDimension <: RiftTypedDimension[_]](dimension: RiftDimension, channel: RiftChannel, toolGroup: Option[ToolGroup] = None): Option[Dematerializer[TChannel, TDimension]]
  def tryGetDematerializer[TChannel <: RiftChannel, TDimension <: RiftTypedDimension[_]](implicit md: Manifest[TDimension], mc: Manifest[TChannel]): Option[Dematerializer[TChannel,TDimension]]

  def addCanDematerializePrimitiveMA[M[_], A, TChannel <: RiftChannel, TDimension <: RiftDimension](cdsma: CanDematerializePrimitiveMA[M, A, TChannel, TDimension]): Unit
  def tryGetCanDematerializePrimitiveMA[M[_], A, TChannel <: RiftChannel, TDimension <: RiftDimension](implicit mM: Manifest[M[_]] , mA: Manifest[A], mC: Manifest[TChannel], mD: Manifest[TDimension]): Option[CanDematerializePrimitiveMA[M, A, TChannel, TDimension]] =
    tryGetCanDematerializePrimitiveMAByTypes(mM.erasure, mA.erasure, mC.erasure.asInstanceOf[Class[_ <: RiftChannel]], mD.erasure.asInstanceOf[Class[_ <: RiftDimension]]).map(_.asInstanceOf[CanDematerializePrimitiveMA[M, A, TChannel, TDimension]])
  def tryGetCanDematerializePrimitiveMAByTypes(tM: Class[_] , tA: Class[_], tChannel: Class[_ <: RiftChannel], tDimension: Class[_ <: RiftDimension]): Option[AnyRef]
}