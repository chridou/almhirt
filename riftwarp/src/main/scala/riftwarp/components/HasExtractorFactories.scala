package riftwarp.components

import scala.reflect.ClassTag
import riftwarp._

trait HasExtractorFactories {
  def addExtractorFactory(arrayFactory: ExtractorFactory[_ <: RiftDimension], isChannelDefault: Boolean = false): Unit
  def addExtractorFactoryAsDefault(arrayFactory: ExtractorFactory[_ <: RiftDimension]) = addExtractorFactory(arrayFactory, true)
  def tryGetExtractorFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None): Option[ExtractorFactory[RiftDimension]]
  def tryGetExtractorFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: ClassTag[TDimension]) =
    tryGetExtractorFactoryByType(mD.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]])(channel, toolGroup)
}