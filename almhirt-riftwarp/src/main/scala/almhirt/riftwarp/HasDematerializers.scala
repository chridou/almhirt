package almhirt.riftwarp

trait HasDematerializers {
  def addDematerializer[D <: Dematerializer[_,_], TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel,To], asChannelDefault: Boolean = false)(implicit m: Manifest[To])
  def addDematerializerAsDefault[D <: Dematerializer[_,_], TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel,To])(implicit m: Manifest[To]) = addDematerializer(dematerializer, true)

  def tryGetDematerializerByDescriptor[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[_, To]]
  def tryGetDematerializer[TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](implicit md: Manifest[To], mc: Manifest[TChannel]): Option[Dematerializer[TChannel,To]]

  def addCanDematerializePrimitiveMA[M[_], A, TChannel <: RiftChannelDescriptor, TDimension <: RiftDimension](cdsma: CanDematerializePrimitiveMA[M, A, TChannel, TDimension]): Unit
  def tryGetCanDematerializePrimitiveMA[M[_], A, TChannel <: RiftChannelDescriptor, TDimension <: RiftDimension](implicit mM: Manifest[M[_]] , mA: Manifest[A], mC: Manifest[TChannel], mD: Manifest[TDimension]): Option[CanDematerializePrimitiveMA[M, A, TChannel, TDimension]] =
    tryGetCanDematerializePrimitiveMAByTypes(mM.erasure, mA.erasure, mC.erasure.asInstanceOf[Class[_ <: RiftChannelDescriptor]], mD.erasure.asInstanceOf[Class[_ <: RiftDimension]]).map(_.asInstanceOf[CanDematerializePrimitiveMA[M, A, TChannel, TDimension]])
  def tryGetCanDematerializePrimitiveMAByTypes(tM: Class[_] , tA: Class[_], tChannel: Class[_ <: RiftChannelDescriptor], tDimension: Class[_ <: RiftDimension]): Option[AnyRef]
}