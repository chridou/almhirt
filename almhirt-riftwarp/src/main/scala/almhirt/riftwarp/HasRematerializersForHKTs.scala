package almhirt.riftwarp

trait HasRematerializersForHKTs {
  def addCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftTypedDimension[_], TChannel <: RiftChannel](crsma: CanRematerializePrimitiveMA[M, A, TDimension, TChannel]): Unit
  def tryGetCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftTypedDimension[_], TChannel <: RiftChannel](implicit mM: Manifest[M[_]] , mA: Manifest[A], mD: Manifest[TDimension], mC: Manifest[TChannel]): Option[CanRematerializePrimitiveMA[M, A, TDimension, TChannel]] =
    tryGetCanRematerializePrimitiveMAByTypes(mM.erasure, mA.erasure, mD.erasure.asInstanceOf[Class[_ <: RiftTypedDimension[_]]], mC.erasure.asInstanceOf[Class[_ <: RiftChannel]]).map(_.asInstanceOf[CanRematerializePrimitiveMA[M, A, TDimension, TChannel]])
  def tryGetCanRematerializePrimitiveMAByTypes(tM: Class[_] , tA: Class[_], tDimension: Class[_ <: RiftTypedDimension[_]], tChannel: Class[_ <: RiftChannel]): Option[AnyRef]
}