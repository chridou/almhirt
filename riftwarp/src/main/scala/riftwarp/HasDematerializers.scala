package riftwarp

trait HasDematerializers {
  def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean = false)
  def addDematerializerFactoryAsDefault(factory: DematerializerFactory[_ <: RiftDimension]) = addDematerializerFactory(factory, true)

  def tryGetDematerializerFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit md: Manifest[TDimension]): Option[DematerializerFactory[TDimension]]
}