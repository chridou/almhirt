package almhirt.riftwarp

trait HasRematerializationArrayFactories {
  def addArrayFactory(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension], isChannelDefault: Boolean = false): Unit
  def addArrayFactoryAsDefault(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension]) = addArrayFactory(arrayFactory, true)
  def tryGetArrayFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]): Option[RematerializationArrayFactory[TDimension]]

  def addCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension](crsma: CanRematerializePrimitiveMA[M, A, TDimension]): Unit
  def tryGetCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension](channel: RiftChannel)(implicit mM: Manifest[M[_]] , mA: Manifest[A], mD: Manifest[TDimension]): Option[CanRematerializePrimitiveMA[M, A, TDimension]] =
    tryGetCanRematerializePrimitiveMAByTypes(mM.erasure, mA.erasure, mD.erasure.asInstanceOf[Class[_ <: RiftDimension]], channel: RiftChannel).map(_.asInstanceOf[CanRematerializePrimitiveMA[M, A, TDimension]])
  def tryGetCanRematerializePrimitiveMAByTypes(tM: Class[_] , tA: Class[_], tDimension: Class[_ <: RiftDimension], channel: RiftChannel): Option[AnyRef]

}