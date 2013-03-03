package riftwarp.components

import scala.reflect.ClassTag
import riftwarp._

trait HasWarpSequencers {
  def addWarpSequencerFactory(factory: WarpSequencerFactory[_ <: RiftDimension], asChannelDefault: Boolean = false)
  def addWarpSequencerFactoryAsDefault(factory: WarpSequencerFactory[_ <: RiftDimension]) = addWarpSequencerFactory(factory, true)

  def tryGetWarpSequencerFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None): Option[WarpSequencerFactory[_ <: RiftDimension]]
  def tryGetWarpSequencerFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit md: ClassTag[TDimension]) =
    tryGetWarpSequencerFactoryByType(md.runtimeClass.asInstanceOf[Class[TDimension]])(channel, toolGroup).map(_.asInstanceOf[WarpSequencerFactory[TDimension]])
}