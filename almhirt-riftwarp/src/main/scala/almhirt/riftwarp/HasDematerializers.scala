package almhirt.riftwarp

trait HasDematerializers {
  def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean = false)
  def addDematerializerFactoryAsDefault(factory: DematerializerFactory[_ <: RiftDimension]) = addDematerializerFactory(factory, true)

  def tryGetDematerializerFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit md: Manifest[TDimension]): Option[DematerializerFactory[TDimension]]

  def addCanDematerializePrimitiveMA[M[_], A](cdsma: CanDematerializePrimitiveMA[M, A, _ <: RiftDimension]): Unit
  def tryGetCanDematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension](channel: RiftChannel)(implicit mM: Manifest[M[_]], mA: Manifest[A], mD: Manifest[TDimension]): Option[CanDematerializePrimitiveMA[M, A, TDimension]] =
    tryGetCanDematerializePrimitiveMAByTypes(mM.erasure, mA.erasure, channel, mD.erasure.asInstanceOf[Class[_ <: RiftDimension]]).map(_.asInstanceOf[CanDematerializePrimitiveMA[M, A, TDimension]])
  def tryGetCanDematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], channel: RiftChannel, tDimension: Class[_ <: RiftDimension]): Option[AnyRef]
}