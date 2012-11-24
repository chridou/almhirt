package almhirt.riftwarp

trait HasRematerializersForHKTs {
  def addCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension, TChannel <: RiftChannelDescriptor](crsma: CanRematerializePrimitiveMA[M, A, TDimension, TChannel]): Unit
  def tryGetCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension, TChannel <: RiftChannelDescriptor](implicit mM: Manifest[M[_]] , mA: Manifest[A], mD: Manifest[TDimension], mC: Manifest[TChannel]): Option[CanDematerializePrimitiveMA[M, A, TDimension, TChannel]] =
    tryGetCanRematerializePrimitiveMAByTypes(mM.erasure, mA.erasure, mD.erasure.asInstanceOf[Class[_ <: RiftDimension]], mC.erasure.asInstanceOf[Class[_ <: RiftChannelDescriptor]]).map(_.asInstanceOf[CanDematerializePrimitiveMA[M, A, TDimension, TChannel]])
  def tryGetCanRematerializePrimitiveMAByTypes(tM: Class[_] , tA: Class[_], tDimension: Class[_ <: RiftDimension], tChannel: Class[_ <: RiftChannelDescriptor]): Option[AnyRef]
}