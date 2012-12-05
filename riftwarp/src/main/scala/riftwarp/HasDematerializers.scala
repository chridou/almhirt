package riftwarp

trait HasDematerializers {
  def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean = false)
  def addDematerializerFactoryAsDefault(factory: DematerializerFactory[_ <: RiftDimension]) = addDematerializerFactory(factory, true)

  def tryGetDematerializerFactoryByType(tDimemsion: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None): Option[DematerializerFactory[_ <: RiftDimension]]
  def tryGetDematerializerFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit md: Manifest[TDimension]) =
    tryGetDematerializerFactoryByType(md.erasure.asInstanceOf[Class[TDimension]])(channel, toolGroup).map(_.asInstanceOf[DematerializerFactory[TDimension]])
}